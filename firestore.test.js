const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Sipho Khumalo",
      email: "sipho@mzansi.co.za",
      photoUrl: null,
      coins: 150,
      unlockedPacks: ["free_classic", "free_slang"],
      gamesPlayed: 0,
      totalCorrect: 0,
      highScore: 0,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(profileRef.get());
});

test("Cross-user access: Bob cannot read or modify Alice's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      coins: 150,
      unlockedPacks: ["free_classic"],
      gamesPlayed: 1,
      totalCorrect: 5,
      highScore: 5,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      coins: 9999,
    })
  );
});

test("Validation: Reject profile with negative coins or ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertFails(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Sipho",
      coins: -50,
      unlockedPacks: ["free_classic"],
      gamesPlayed: 0,
      totalCorrect: 0,
      highScore: 0,
      createdAt: new Date(),
    })
  );

  await assertFails(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Sipho",
      coins: 150,
      unlockedPacks: ["free_classic"],
      gamesPlayed: 0,
      totalCorrect: 0,
      highScore: 0,
      createdAt: new Date(),
      isAdmin: true,
    })
  );
});

test("Game sessions: User can create session in new blueprint modes, but cannot modify past sessions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sessionRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("game_sessions")
    .doc("session_001");

  await assertSucceeds(
    sessionRef.set({
      userId: ALICE_UID,
      deckId: "deck_tiktok_lockdown",
      deckName: "TikTok Trends & Lockdown Vault",
      gameMode: "my_reaction",
      teamAScore: 7,
      teamBScore: 5,
      wordsGuessed: ["Bacardi Dance", "My Reaction Facial Ranking"],
      wordsPassed: ["Shoe Kick Transition"],
      durationSeconds: 60,
      createdAt: new Date(),
    })
  );

  await assertFails(
    sessionRef.update({
      teamAScore: 10,
    })
  );
});

test("Custom cards: User can create custom cards and list them", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("custom_cards")
    .doc("card_001");

  await assertSucceeds(
    cardRef.set({
      userId: ALICE_UID,
      term: "Lotto Winning Numbers Song",
      category: "2000s TV & Jingles",
      hints: ["Tata ma chance", "Tata ma millions", "Draw night"],
      culturalEra: "Early 2000s",
      imageUrl: null,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_cards").get()
  );
});

test("Payment methods: User can add, update default status, and list their payment methods, while cross-user access is blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const methodRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("payment_methods")
    .doc("pm_visa_4242");

  await assertSucceeds(
    methodRef.set({
      userId: ALICE_UID,
      type: "card",
      brand: "Visa",
      last4: "4242",
      expiryMonth: 8,
      expiryYear: 2029,
      holderName: "Sipho Khumalo",
      isDefault: true,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    methodRef.update({
      isDefault: false,
    })
  );

  // Reject ghost fields on payment method
  await assertFails(
    methodRef.update({
      isVerified: true,
    })
  );

  // Bob cannot read Alice's payment methods
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("payment_methods")
      .doc("pm_visa_4242")
      .get()
  );
});

test("Payment transactions: User can create ZAR transaction receipt, but cannot modify or delete it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions")
    .doc("tx_001");

  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      itemId: "coins_300",
      itemTitle: "Township Stash (300 Coins)",
      amountCents: 2999,
      currency: "ZAR",
      paymentMethodId: "pm_visa_4242",
      paymentMethodSummary: "Visa •••• 4242",
      coinsGranted: 300,
      packUnlocked: null,
      status: "succeeded",
      createdAt: new Date(),
    })
  );

  // Transactions are immutable
  await assertFails(
    txRef.update({
      amountCents: 0,
    })
  );
  await assertFails(txRef.delete());
});

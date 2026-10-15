const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

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

test("Unauthenticated user: cannot read applicants", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("applicants").get());
});

test("Alice cannot access Bob's applicants", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("applicants").doc("app_bob_1").set({
      userId: BOB_UID,
      registrationNumber: "SPMB-2027-001",
      fullName: "Bob Junior",
      gender: "Laki-laki",
      schoolOrigin: "SMP 1",
      phone: "08123456789",
      majorCode: "TKJ",
      verificationStatus: "Terverifikasi",
      admissionStatus: "Diterima",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("applicants").doc("app_bob_1").get());
});

test("Alice can create and read her own applicant", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("applicants").doc("app_alice_1").set({
      userId: ALICE_UID,
      registrationNumber: "SPMB-2027-002",
      fullName: "Alice Student",
      gender: "Perempuan",
      schoolOrigin: "SMP 2",
      phone: "08123456780",
      majorCode: "DKV",
      verificationStatus: "Menunggu verifikasi",
      admissionStatus: "Menunggu keputusan",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("applicants").doc("app_alice_1").get()
  );
});

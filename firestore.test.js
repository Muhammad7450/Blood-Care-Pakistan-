const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_donor_123";
const BOB_UID = "bob_patient_456";
const CHARLIE_UID = "charlie_stranger_789";

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

// --- UNATHENTICATED REJECTION TESTS ---
test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("Unauthenticated user: cannot create blood requests", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("bloodRequests").doc("req_1").set({
    patientId: "some_user",
    patientName: "Patient",
    bloodGroup: "A+",
    city: "Nawabshah",
    normalizedCity: "nawabshah",
    hospitalName: "Civil Hospital",
    unitsRequired: 2,
    urgency: "CRITICAL",
    status: "OPEN",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

// --- USER PROFILE SECURITY ---
test("Authenticated user: can create their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    uid: ALICE_UID,
    fullName: "Alice Donor",
    role: "DONOR",
    city: "Nawabshah",
    normalizedCity: "nawabshah",
    bloodGroup: "A+",
    isDonor: true,
    isAvailable: true,
    isProfileVerified: false,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Authenticated user: cannot create someone else's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    uid: BOB_UID,
    fullName: "Bob Spoofed",
    role: "PATIENT",
    city: "Karachi",
    normalizedCity: "karachi",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

// --- BLOOD REQUESTS SECURITY ---
test("Patient: can create an OPEN blood request for themselves", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("bloodRequests").doc("req_nawabshah_1").set({
    patientId: BOB_UID,
    patientName: "Bob Patient",
    bloodGroup: "A+",
    city: "Nawabshah",
    normalizedCity: "nawabshah",
    hospitalName: "Peoples Medical Hospital",
    unitsRequired: 2,
    urgency: "CRITICAL",
    status: "OPEN",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Donor: cannot modify patient's request details", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bloodRequests").doc("req_nawabshah_1").set({
      patientId: BOB_UID,
      patientName: "Bob Patient",
      bloodGroup: "A+",
      city: "Nawabshah",
      normalizedCity: "nawabshah",
      hospitalName: "Peoples Medical Hospital",
      unitsRequired: 2,
      urgency: "CRITICAL",
      status: "OPEN",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("bloodRequests").doc("req_nawabshah_1").update({
    hospitalName: "Tampered Hospital",
    updatedAt: new Date()
  }));
});

// --- DONOR RESPONSE SECURITY ---
test("Donor: can submit a response to an open request", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("donorResponses").doc("resp_1").set({
    requestId: "req_nawabshah_1",
    donorId: ALICE_UID,
    patientId: BOB_UID,
    donorName: "Alice Donor",
    donorBloodGroup: "A+",
    responseStatus: "ACCEPTED",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Third party: cannot delete donor response", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("donorResponses").doc("resp_1").set({
      requestId: "req_nawabshah_1",
      donorId: ALICE_UID,
      patientId: BOB_UID,
      donorName: "Alice Donor",
      responseStatus: "ACCEPTED",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("donorResponses").doc("resp_1").delete());
});

// --- NOTIFICATION SECURITY ---
test("Recipient: can read their own notifications, others cannot", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("notifications").doc("notif_alice").set({
      recipientUserId: ALICE_UID,
      requestId: "req_nawabshah_1",
      title: "URGENT BLOOD REQUEST - A+ Nawabshah",
      body: "Peoples Medical Hospital needs 2 units of A+",
      type: "NEW_REQUEST",
      isRead: false,
      createdAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("notifications").doc("notif_alice").get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("notifications").doc("notif_alice").get());
});

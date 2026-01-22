describe("Booking", () => {
  const email = "client@test.com";
  const pass = "Passw0rd!";

  const isoTomorrow = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split("T")[0];
  };

beforeEach(() => {
    // --- RESET ---
  cy.request("POST", "http://localhost:8080/api/test/reset");

  // --- COACH SETUP ---
  const coachEmail = "coach@test.com";
  const coachPass = "Passw0rd!";

  cy.registerCoach(coachEmail, coachPass);


  cy.intercept("POST", "**/auth/login").as("coachLogin");
  cy.intercept("GET", "**/auth/me").as("coachMe");

  cy.visit("/login");
  cy.get("#email").clear().type(coachEmail);
  cy.get("#password").clear().type(coachPass);
  cy.contains('button[type="submit"]', /sign in/i).click();

  cy.wait("@coachLogin").its("response.statusCode").should("be.oneOf", [200, 204]);
  cy.wait("@coachMe").its("response.statusCode").should("eq", 200);


  cy.createMyAvailability({
    date: isoTomorrow(),
    startTime: "09:00",
    endTime: "12:00",
  });


  cy.clearCookies();
  cy.clearLocalStorage();

  // --- CLIENT SETUP ---
  cy.registerClient("client@test.com", "Passw0rd!");
  
  cy.intercept("POST", "**/auth/login").as("login");
  cy.intercept("GET", "**/auth/me").as("me");

  cy.visit("/login");
  cy.get("#email").clear().type("client@test.com");
  cy.get("#password").clear().type("Passw0rd!");
  cy.contains('button[type="submit"]', /sign in/i).click();


  cy.wait("@login").its("response.statusCode").should("be.oneOf", [200, 204]);


  cy.wait("@me").its("response.statusCode").should("eq", 200);
});




  it("client can book an appointment from available slots", () => {

    cy.request("http://localhost:8080/api/coaches").then((res) => {
  cy.log(JSON.stringify(res.body));
});

    cy.visit("/book-a-coach");


    cy.contains("h1", /book a session/i).should("be.visible");


    cy.get("select#coach").should("be.enabled");


    cy.get('button[type="button"][aria-pressed]')
      .should("have.length.greaterThan", 1);

    cy.get('button[type="button"][aria-pressed]')
      .eq(1)
      .click();


    cy.contains(/loading times or no available slots/i).should("be.visible");


    cy.contains(/loading times or no available slots/i, { timeout: 15000 })
      .should("not.exist");


    cy.get("section")
      .contains(/available times/i)
      .parent()
      .within(() => {
        cy.get('button[type="button"]').should("have.length.greaterThan", 0);
        cy.get('button[type="button"]').first().click();
      });

    // Submit: "Book session" (type=submit)
    cy.get('button[type="submit"]')
      .should("be.enabled")
      .and("contain.text", "Book")
      .click();


    cy.contains(/your session is booked for/i, { timeout: 15000 }).should(
      "be.visible"
    );
  });
});

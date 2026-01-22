describe("Auth", () => {
  it("can login and reach dashboard", () => {
    const email = `client+${Date.now()}@test.com`;
    const password = "Passw0rd!";

    cy.registerClient(email, password);
    cy.loginUI(email, password);

    cy.url().should("include", "/");
    cy.contains(/home/i);
  });

  it("blocks dashboard when not authenticated", () => {
    cy.clearCookies();
    cy.visit("/dashboard");
    cy.url().should("include", "/login");
  });
});

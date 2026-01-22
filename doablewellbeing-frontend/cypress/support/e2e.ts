import './commands'

beforeEach(() => {
  cy.request("POST", `${Cypress.env("apiBaseUrl")}/api/test/reset`);
});

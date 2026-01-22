
declare global {
  namespace Cypress {
    interface Chainable {
      registerClient(email: string, password: string): Chainable<any>;
      registerCoach(email: string, password: string): Chainable<any>;
      loginUI(email: string, password: string): Chainable<void>;
      logoutUI(): Chainable<void>;
      registerCoach(email: string, password: string): Chainable<any>;
      createMyAvailability(req: {
        date: string;      
        startTime: string; 
        endTime: string;   
      }): Chainable<any>;
    }
  }
}

export {};

Cypress.Commands.add("registerClient", (email: string, password: string) => {
  return cy.request("POST", `${Cypress.env("apiBaseUrl")}/auth/register`, {
    email,
    password,
    firstName: "Client",
    lastName: "Test",
  });
});

Cypress.Commands.add("loginUI", (email: string, password: string) => {
  cy.visit("/login");
  cy.get("#email").clear().type(email);
  cy.get("#password").clear().type(password);
  cy.contains('button[type="submit"]', /sign in/i).click();
});

Cypress.Commands.add("registerCoach", (email: string, password: string) => {
  return cy.request("POST", `${Cypress.env("apiBaseUrl")}/auth/registerCoach`, {
    email,
    password,
    firstName: "Coach",
    lastName: "Test",
  });
});

Cypress.Commands.add("logoutUI", () => {
  cy.request("POST", `${Cypress.env("apiBaseUrl")}/auth/logout`);
});



function getCsrfToken(): Cypress.Chainable<string> {
  return cy
    .request({
      method: "GET",
      url: `${Cypress.env("apiBaseUrl")}/csrf-token`,
      failOnStatusCode: false,
    })
    .then((r) => {
      const body: any = r.body;
      if (typeof body === "string") return body;
      if (body && typeof body.token === "string") return body.token;

    
      return "";
    });
}

Cypress.Commands.add("createMyAvailability", (req) => {

  const path = "/api/coach/availabilities/me";

  return getCsrfToken().then((csrf) => {
    return cy.request({
      method: "POST",
      url: `${Cypress.env("apiBaseUrl")}${path}`,
      headers: csrf ? { "X-CSRF-TOKEN": csrf } : undefined,
      body: {
      
        date: req.date,         
        startTime: req.startTime, 
        endTime: req.endTime,     
      },
    });
  });
});
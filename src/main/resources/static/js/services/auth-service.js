class AuthService {
  static LOGIN_URL = "/auth/login";
  static REDIRECT_URL = "/";
  static VALIDATION_URL = "/api/v1/auth/validate";
  static isInitializing = false;

  static async init() {
    if (this.isInitializing) return;
    this.isInitializing = true;

    try {
      this.setupFetchInterceptor();

      // Skip auth check on login page
      if (window.location.pathname === this.LOGIN_URL) return;
      if (window.location.pathname.includes("/auth")) return;

      this.validateToken().then((isValid) => {
        if (!isValid) {
          this.redirectToLogin();
        } else {
          sessionStorage.setItem("authenticated", "true");
        }
      });
    } catch (error) {
      console.error("Auth initialization failed:", error);
      this.redirectToLogin();
    } finally {
      this.isInitializing = false;
    }
  }

  static redirectToLogin() {
    const currentPath = window.location.pathname;
    if (currentPath !== this.LOGIN_URL) {
      const redirectUrl = encodeURIComponent(currentPath);
      window.location.replace(`${this.LOGIN_URL}?redirect=${redirectUrl}`);
    }
  }

  // Helper function to set JWT_TOKEN cookie
  static setTokenCookie(JWT_TOKEN) {
    const secure = window.location.protocol === "https:" ? "; Secure" : "";
    document.cookie = `JWT_TOKEN=${JWT_TOKEN}; path=/; SameSite=Lax${secure}`;
  }

  // Helper function to get JWT_TOKEN from cookie
  static getTokenFromCookie() {
    const name = "JWT_TOKEN=";
    const decodedCookie = decodeURIComponent(document.cookie);
    console.log("Decoded cookie:", decodedCookie); // Debug line

    const cookies = decodedCookie.split(";").map((c) => c.trim());
    for (let cookie of cookies) {
      if (cookie.startsWith(name)) {
        const token = cookie.substring(name.length);
        console.log("Found token:", token); // Debug line
        return token;
      }
    }
    console.log("No JWT_TOKEN found in cookies"); // Debug line
    return "";
  }

  // Helper function to delete JWT_TOKEN cookie
  static deleteTokenCookie() {
    document.cookie =
      "JWT_TOKEN=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
  }

  static async validateToken() {
    try {
      const response = await fetch("/api/v1/auth/validate", {
        credentials: "include", // Ensure cookies are sent
      });
      const data = await response.json();
      return data.valid === true;
    } catch (error) {
      console.error("JWT_TOKEN validation failed:", error);
      return false;
    }
  }

  static async login(username, password) {
    try {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
        credentials: "include",
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || "Login failed");
      }

      const data = await response.json();
      console.log("Login response:", data); // Debug logging

      // Update to use token from response
      if (data.JWT_TOKEN) {
        console.log("Setting token:", data.JWT_TOKEN); // Debug logging
        this.setTokenCookie(data.JWT_TOKEN);
      }

      const redirectUrl =
        new URLSearchParams(window.location.search).get("redirect") ||
        this.REDIRECT_URL;
      window.location.href = redirectUrl;
      return true;
    } catch (error) {
      console.error("Login failed:", error);
      throw error;
    }
  }

  static logout() {
    this.deleteTokenCookie();
    sessionStorage.removeItem("authenticated");
    window.location.href = this.LOGIN_URL;
  }

  static setupFetchInterceptor() {
    const originalFetch = window.fetch;
    window.fetch = async function (...args) {
      if (!args[1]) args[1] = {};
      if (!args[1].headers) args[1].headers = {};

      // Extract the request URL safely
      let requestUrl = "";
      if (typeof args[0] === "string") {
        requestUrl = args[0];
      } else if (args[0] instanceof Request) {
        requestUrl = args[0].url;
      }

      // Check if this is an external URL
      const isExternalUrl =
        requestUrl.startsWith("http") &&
        !requestUrl.includes(window.location.host);

      // Don't modify requests to external URLs
      if (isExternalUrl) {
        return originalFetch.apply(this, args);
      }

      const isAuthEndpoint =
        requestUrl.includes("/auth/") || requestUrl.includes("/api/v1/auth/");

      if (!isAuthEndpoint) {
        // Remove adding Authorization header
        args[1].headers = {
          ...args[1].headers,
          Accept: "application/json",
        };
      }

      args[1].credentials = "include"; // Include cookies in requests

      return originalFetch.apply(this, args);
    };
  }
}

// Initialize the authentication service
AuthService.init();

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

  // Helper function to set token cookie
  static setTokenCookie(token) {
    document.cookie = `token=${token}; path=/;`;
  }

  // Helper function to get token from cookie
  static getTokenFromCookie() {
    const name = "token=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(";");
    for (let c of ca) {
      while (c.charAt(0) === " ") c = c.substring(1);
      if (c.indexOf(name) === 0) return c.substring(name.length, c.length);
    }
    return "";
  }

  // Helper function to delete token cookie
  static deleteTokenCookie() {
    document.cookie = "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
  }

  static async validateToken() {
    const token = this.getTokenFromCookie();
    if (!token) return false;

    try {
      const response = await fetch("/api/v1/auth/validate", {
        headers: {
          Authorization: "Bearer " + token,
        },
      });
      const data = await response.json();
      return data.valid === true;
    } catch (error) {
      console.error("Token validation failed:", error);
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

      // Save token to cookie for future requests
      if (data.token) {
        this.setTokenCookie(data.token);
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

  static getAuthHeader() {
    const token = this.getTokenFromCookie();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  static logout() {
    this.deleteTokenCookie();
    sessionStorage.removeItem("authenticated");
    window.location.href = this.LOGIN_URL;
  }

  // Modify fetch interceptor to handle both cookie and header auth
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
        const headers = AuthService.getAuthHeader();
        args[1].headers = {
          ...args[1].headers,
          ...headers,
          Accept: "application/json",
        };
      }

      args[1].credentials = "include"; // Include credentials for same-origin requests

      return originalFetch.apply(this, args);
    };
  }
}

// Initialize the authentication service
AuthService.init();

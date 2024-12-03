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

  static async validateToken() {
    try {
      const response = await fetch(this.VALIDATION_URL, {
        method: "GET",
        cache: "no-store",
        credentials: "include",
        headers: {
          ...this.getAuthHeader(),
          Accept: "application/json",
        },
      });

      if (!response.ok) return false;

      const data = await response.json();
      if (data.valid) {
        // Ensure token is stored if validation successful
        const token = response.headers.get("Authorization");
        if (token) {
          sessionStorage.setItem("jwt_token", token.replace("Bearer ", ""));
        }
      }
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

      // Save token to sessionStorage for future requests
      if (data.token) {
        sessionStorage.setItem("jwt_token", data.token);
      }

      window.location.href = data.redirectUrl || this.REDIRECT_URL;
      return true;
    } catch (error) {
      console.error("Login failed:", error);
      throw error;
    }
  }

  static getAuthHeader() {
    const token = sessionStorage.getItem("jwt_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  static logout() {
    sessionStorage.removeItem("jwt_token");
    sessionStorage.removeItem("authenticated");
    window.location.href = this.LOGIN_URL;
  }

  // Modify fetch interceptor to handle both cookie and header auth
  static setupFetchInterceptor() {
    const originalFetch = window.fetch;
    window.fetch = async function (...args) {
      if (!args[1]) args[1] = {};
      if (!args[1].headers) args[1].headers = {};

      // Don't add auth headers for auth endpoints
      const isAuthEndpoint =
        args[0].includes("/auth/") || args[0].includes("/api/v1/auth/");

      if (!isAuthEndpoint) {
        const headers = AuthService.getAuthHeader();
        args[1].headers = {
          ...args[1].headers,
          ...headers,
          Accept: "application/json",
        };
      }

      args[1].credentials = "include"; // Always include credentials

      return originalFetch.apply(this, args);
    };
  }
}

// Initialize the authentication service
AuthService.init();

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function normalizeToken(raw) {
  if (!raw) {
    return "";
  }

  const trimmed = String(raw).trim();
  return trimmed.toLowerCase().startsWith("bearer ") ? trimmed.slice(7) : trimmed;
}

async function callApi(path, { method = "GET", token, body, fallbackPath } = {}) {
  const headers = {
    "Content-Type": "application/json"
  };

  if (token) {
    headers.Authorization = `Bearer ${normalizeToken(token)}`;
  }

  const request = {
    method,
    headers
  };

  if (body !== undefined) {
    request.body = JSON.stringify(body);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, request);

  if (!response.ok && fallbackPath) {
    const fallbackResponse = await fetch(`${API_BASE_URL}${fallbackPath}`, request);
    return parseResponse(fallbackResponse);
  }

  return parseResponse(response);
}

async function parseResponse(response) {
  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    if (response.status === 500) {
      throw new Error("500 internal server error.");
    }

    let message = "";

    if (typeof data === "object" && data !== null) {
      const errorList = data.ErrorList ?? data.errorList;
      const errorListText =
        Array.isArray(errorList) && errorList.length
          ? errorList.map((item) => (typeof item === "string" ? item : JSON.stringify(item))).join(" | ")
          : "";

      message =
        data.ErrorMessage ||
        data.errorMessage ||
        errorListText ||
        data.message ||
        data.error ||
        data.description ||
        JSON.stringify(data);
    } else if (typeof data === "string") {
      message = data;
    }

    if (!message) {
      message = `Request failed with status ${response.status}`;
    }

    throw new Error(message);
  }

  return data;
}

export const AuthApi = {
  register(payload) {
    return callApi("/api/users/register", { method: "POST", body: payload });
  },

  login(payload) {
    return callApi("/api/users/login", { method: "POST", body: payload });
  },

  getMe(token) {
    return callApi("/api/users/me", { token });
  }
};

export const WalletApi = {
  addMoney(token, amount) {
    return callApi("/api/wallets/me/addMoney", {
      method: "POST",
      token,
      body: { amount: Number(amount) }
    });
  },

  getMyWallet(token) {
    return callApi("/api/wallets/me", { token });
  },

  getMyBalance(token) {
    return callApi("/api/wallets/me/balance", { token });
  }
};

export const MerchantApi = {
  createMerchant(token, payload) {
    return callApi("/api/merchants/register", { method: "POST", token, body: payload });
  },

  getMerchantByUserId(token, userId) {
    return callApi(`/api/merchants/internal/user/${userId}`, { token });
  },

  getMerchantById(token, merchantId) {
    return callApi(`/api/merchants/${merchantId}`, { token });
  }
};

export const TransactionApi = {
  getMyHistory(token, page = 0, size = 10) {
    return callApi(`/api/transactions/me/history?page=${page}&size=${size}`, { token });
  },

  meTransfer(token, payload) {
    return callApi("/api/transactions/meTransfer", {
      method: "POST",
      token,
      body: payload,
      fallbackPath: "/api/transactions/me/transfer"
    });
  },

  payMerchant(token, payload) {
    return callApi("/api/transactions/me/pay-merchant", {
      method: "POST",
      token,
      body: payload
    });
  }
};

export { API_BASE_URL, normalizeToken };

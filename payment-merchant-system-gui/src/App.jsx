import React, { useEffect, useState } from "react";
import { API_BASE_URL, AuthApi, MerchantApi, TransactionApi, WalletApi, normalizeToken } from "./api";

const TOKEN_KEY = "pms_gui_token";

function getStoredToken() {
  return normalizeToken(localStorage.getItem(TOKEN_KEY));
}

function App() {
  const [token, setToken] = useState(getStoredToken());
  const [mode, setMode] = useState("login");
  const [busy, setBusy] = useState(false);
  const [banner, setBanner] = useState({ type: "", text: "" });

  const [registerForm, setRegisterForm] = useState({ username: "", password: "", email: "", fullName: "" });
  const [loginForm, setLoginForm] = useState({ username: "", password: "" });

  const [me, setMe] = useState(null);
  const [wallet, setWallet] = useState(null);
  const [walletBalance, setWalletBalance] = useState("-");
  const [addMoneyAmount, setAddMoneyAmount] = useState("");

  const [mappedMerchantId, setMappedMerchantId] = useState(null);
  const [createMerchantForm, setCreateMerchantForm] = useState({
    merchantCode: "",
    businessName: "",
    email: ""
  });

  const [transferForm, setTransferForm] = useState({ toWalletId: "", amount: "", description: "" });
  const [payMerchantForm, setPayMerchantForm] = useState({ merchantId: "", amount: "", description: "" });

  const isLoggedIn = Boolean(token);

  useEffect(() => {
    if (!banner.text) {
      return;
    }

    const timer = setTimeout(() => {
      setBanner({ type: "", text: "" });
    }, 5000);

    return () => clearTimeout(timer);
  }, [banner]);

  function showPopup(message) {
    setBanner({ type: "info", text: message || "Operation completed." });
  }

  useEffect(() => {
    if (!token) {
      setMe(null);
      setWallet(null);
      setWalletBalance("-");
      setMappedMerchantId(null);
      return;
    }
    hydrateUserProfile(token);
    hydrateDashboard(token);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  async function resolveMappedMerchantId(currentToken, userId) {
    if (!currentToken || !userId) {
      return null;
    }

    try {
      const merchantResponse = await MerchantApi.getMerchantByUserId(currentToken, userId);
      return merchantResponse?.id ? String(merchantResponse.id) : null;
    } catch (error) {
      const message = String(error?.message || "").toLowerCase();
      if (message.includes("not found") || message.includes("404")) {
        return null;
      }
      throw error;
    }
  }

  async function hydrateUserProfile(currentToken) {
    setBusy(true);
    try {
      const [meResponse] = await Promise.all([
        AuthApi.getMe(currentToken)
        //WalletApi.getMyWallet(currentToken),
        //WalletApi.getMyBalance(currentToken)
      ]);

      setMe(meResponse);
      //setWallet(walletResponse);
      //setWalletBalance(balanceResponse);

      const userMappedMerchantId = await resolveMappedMerchantId(currentToken, meResponse.userId);
      setMappedMerchantId(userMappedMerchantId);
      setPayMerchantForm((prev) => ({
        ...prev,
        merchantId: userMappedMerchantId || prev.merchantId
      }));

      showPopup("Dashboard refreshed.");
    } catch (error) {
      showPopup(error.message || "Failed to load dashboard.");
      if (String(error.message || "").toLowerCase().includes("401")) {
        handleLogout();
      }
    } finally {
      setBusy(false);
    }
  }

  async function hydrateDashboard(currentToken) {
    setBusy(true);
    try {
      const [meResponse, walletResponse, balanceResponse] = await Promise.all([
        AuthApi.getMe(currentToken),
        WalletApi.getMyWallet(currentToken),
        WalletApi.getMyBalance(currentToken)
      ]);

      setMe(meResponse);
      setWallet(walletResponse);
      setWalletBalance(balanceResponse);

      const userMappedMerchantId = await resolveMappedMerchantId(currentToken, meResponse.userId);
      setMappedMerchantId(userMappedMerchantId);
      setPayMerchantForm((prev) => ({
        ...prev,
        merchantId: userMappedMerchantId || prev.merchantId
      }));

      showPopup("Dashboard refreshed.");
    } catch (error) {
      showPopup(error.message || "Failed to load dashboard.");
      if (String(error.message || "").toLowerCase().includes("401")) {
        handleLogout();
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setBusy(true);
    try {
      await AuthApi.register(registerForm);
      showPopup("Registration completed. Please login.");
      setMode("login");
      setRegisterForm({ username: "", password: "", email: "", fullName: "" });
    } catch (error) {
      showPopup(error.message || "Registration failed.");
    } finally {
      setBusy(false);
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const rawToken = await AuthApi.login(loginForm);
      const cleanedToken = normalizeToken(rawToken);
      localStorage.setItem(TOKEN_KEY, cleanedToken);
      setToken(cleanedToken);
      setLoginForm({ username: "", password: "" });
      showPopup("Login successful.");
    } catch (error) {
      showPopup(error.message || "Login failed.");
    } finally {
      setBusy(false);
    }
  }

  function handleLogout() {
    localStorage.removeItem(TOKEN_KEY);
    setToken("");
    showPopup("Logged out.");
  }

  async function handleAddMoney(event) {
    event.preventDefault();
    setBusy(true);
    try {
      await WalletApi.addMoney(token, addMoneyAmount);
      setAddMoneyAmount("");
      await hydrateDashboard(token);
      showPopup("Wallet credited successfully.");
    } catch (error) {
      showPopup(error.message || "Unable to add money.");
    } finally {
      setBusy(false);
    }
  }

  async function handleCreateMerchant(event) {
    event.preventDefault();
    if (!me?.userId) {
      showPopup("User context missing.");
      return;
    }

    setBusy(true);
    try {
      const payload = {
        ...createMerchantForm,
        userId: me.userId
      };
      const merchantResponse = await MerchantApi.createMerchant(token, payload);
      setPayMerchantForm((prev) => ({ ...prev, merchantId: String(merchantResponse.id) }));
      setMappedMerchantId(String(merchantResponse.id));

      setCreateMerchantForm({ merchantCode: "", businessName: "", email: "" });
      showPopup("Merchant profile created.");
    } catch (error) {
      showPopup(error.message || "Unable to create merchant.");
    } finally {
      setBusy(false);
    }
  }

  async function handleTransfer(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const response = await TransactionApi.meTransfer(token, {
        toWalletId: Number(transferForm.toWalletId),
        amount: Number(transferForm.amount),
        description: transferForm.description
      });
      setTransferForm({ toWalletId: "", amount: "", description: "" });
      await hydrateDashboard(token);
      showPopup(response?.description || "Transfer successful.");
    } catch (error) {
      showPopup(error.message || "Transfer failed.");
    } finally {
      setBusy(false);
    }
  }

  async function handlePayMerchant(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const response = await TransactionApi.payMerchant(token, {
        merchantId: Number(payMerchantForm.merchantId),
        amount: Number(payMerchantForm.amount),
        description: payMerchantForm.description
      });
      setPayMerchantForm((prev) => ({ ...prev, amount: "", description: "" }));
      await hydrateDashboard(token);
      showPopup(response?.description || "Merchant payment successful.");
    } catch (error) {
      showPopup(error.message || "Merchant payment failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="app-shell">
      <header className="top-bar">
        <div>
          <h1>Payment Merchant System</h1>
          <p>Professional dashboard for users, wallets, merchants, and transactions.</p>
          {isLoggedIn && me?.username && <p>Welcome {me.username} - {me.userId}</p>}
        </div>
        <div className="meta">
          <span>API: {API_BASE_URL}</span>
          {isLoggedIn && (
            <button className="ghost" onClick={handleLogout}>
              Logout
            </button>
          )}
        </div>
      </header>

      {banner.text && (
        <div className="popup-backdrop" onClick={() => setBanner({ type: "", text: "" })}>
          <div className="popup-message" onClick={(event) => event.stopPropagation()} role="alert">
            <span>{banner.text}</span>
            <button className="popup-close" onClick={() => setBanner({ type: "", text: "" })}>
              x
            </button>
          </div>
        </div>
      )}

      {!isLoggedIn ? (
        <section className="auth-wrap">
          <div className="tabs">
            <button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>
              Login
            </button>
            <button className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>
              Register
            </button>
          </div>

          {mode === "login" ? (
            <form className="card form" onSubmit={handleLogin}>
              <h2>Login</h2>
              <label>
                Username
                <input
                  required
                  value={loginForm.username}
                  onChange={(e) => setLoginForm((prev) => ({ ...prev, username: e.target.value }))}
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  required
                  value={loginForm.password}
                  onChange={(e) => setLoginForm((prev) => ({ ...prev, password: e.target.value }))}
                />
              </label>
              <button disabled={busy} type="submit">
                {busy ? "Please wait..." : "Sign In"}
              </button>
            </form>
          ) : (
            <form className="card form" onSubmit={handleRegister}>
              <h2>Create Account</h2>
              <label>
                Full name
                <input
                  required
                  value={registerForm.fullName}
                  onChange={(e) => setRegisterForm((prev) => ({ ...prev, fullName: e.target.value }))}
                />
              </label>
              <label>
                Username
                <input
                  required
                  value={registerForm.username}
                  onChange={(e) => setRegisterForm((prev) => ({ ...prev, username: e.target.value }))}
                />
              </label>
              <label>
                Email
                <input
                  type="email"
                  required
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm((prev) => ({ ...prev, email: e.target.value }))}
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  required
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm((prev) => ({ ...prev, password: e.target.value }))}
                />
              </label>
              <button disabled={busy} type="submit">
                {busy ? "Please wait..." : "Register"}
              </button>
            </form>
          )}
        </section>
      ) : (
        <main className="grid">
          <section className="card">
            <div className="card-header">
              <h2>User Profile</h2>
              <button className="ghost" onClick={() => hydrateUserProfile(token)}>
                Refresh
              </button>
            </div>
            {me ? (
              <dl className="kv-grid">
                <dt>User ID</dt>
                <dd>{me.userId}</dd>
                <dt>Username</dt>
                <dd>{me.username}</dd>
                <dt>Full Name</dt>
                <dd>{me.fullName}</dd>
                <dt>Email</dt>
                <dd>{me.email}</dd>
                <dt>Role</dt>
                <dd>{me.userRole}</dd>
                <dt>Status</dt>
                <dd>{me.userStatus}</dd>
              </dl>
            ) : (
              <p>Loading user details...</p>
            )}
          </section>

          <section className="card">
            <h2>Wallet</h2>
            <div className="stats-row">
              <div className="stat">
                <span>Wallet ID</span>
                <strong>{wallet?.id ?? "-"}</strong>
              </div>
              <div className="stat">
                <span>Balance</span>
                <strong>{walletBalance}</strong>
              </div>
              <div className="stat">
                <span>Status</span>
                <strong>{wallet?.status ?? "-"}</strong>
              </div>
            </div>
            <form className="inline-form" onSubmit={handleAddMoney}>
              <input
                type="number"
                min="1"
                step="0.01"
                placeholder="Amount"
                required
                value={addMoneyAmount}
                onChange={(e) => setAddMoneyAmount(e.target.value)}
              />
              <button disabled={busy} type="submit">
                Add Wallet Money
              </button>
            </form>
          </section>

          <section className="card">
            <h2>Merchant</h2>
            {!mappedMerchantId ? (
              <>
                <p className="muted">No merchant linked to this account yet.</p>
                <form className="form" onSubmit={handleCreateMerchant}>
                  <label>
                    Merchant Code
                    <input
                      required
                      value={createMerchantForm.merchantCode}
                      onChange={(e) =>
                        setCreateMerchantForm((prev) => ({ ...prev, merchantCode: e.target.value }))
                      }
                    />
                  </label>
                  <label>
                    Business Name
                    <input
                      required
                      value={createMerchantForm.businessName}
                      onChange={(e) =>
                        setCreateMerchantForm((prev) => ({ ...prev, businessName: e.target.value }))
                      }
                    />
                  </label>
                  <label>
                    Merchant Email
                    <input
                      type="email"
                      required
                      value={createMerchantForm.email}
                      onChange={(e) => setCreateMerchantForm((prev) => ({ ...prev, email: e.target.value }))}
                    />
                  </label>
                  <button disabled={busy} type="submit">
                    Create Merchant
                  </button>
                </form>

              </>
            ) : (
              <p className="muted">Mapped Merchant ID: {mappedMerchantId}</p>
            )}
          </section>

          <section className="card">
            <h2>Transfer to User</h2>
            <form className="form" onSubmit={handleTransfer}>
              <label>
                Receiver Wallet ID
                <input
                  type="number"
                  min="1"
                  required
                  value={transferForm.toWalletId}
                  onChange={(e) => setTransferForm((prev) => ({ ...prev, toWalletId: e.target.value }))}
                />
              </label>
              <label>
                Amount
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={transferForm.amount}
                  onChange={(e) => setTransferForm((prev) => ({ ...prev, amount: e.target.value }))}
                />
              </label>
              <label>
                Description
                <input
                  value={transferForm.description}
                  onChange={(e) => setTransferForm((prev) => ({ ...prev, description: e.target.value }))}
                />
              </label>
              <button disabled={busy} type="submit">
                Pay Another User
              </button>
            </form>
          </section>

          <section className="card">
            <h2>Pay Merchant</h2>
            <form className="form" onSubmit={handlePayMerchant}>
              <label>
                Merchant ID
                <input
                  type="number"
                  min="1"
                  required
                  value={payMerchantForm.merchantId}
                  onChange={(e) => setPayMerchantForm((prev) => ({ ...prev, merchantId: e.target.value }))}
                />
              </label>
              <label>
                Amount
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={payMerchantForm.amount}
                  onChange={(e) => setPayMerchantForm((prev) => ({ ...prev, amount: e.target.value }))}
                />
              </label>
              <label>
                Description
                <input
                  value={payMerchantForm.description}
                  onChange={(e) => setPayMerchantForm((prev) => ({ ...prev, description: e.target.value }))}
                />
              </label>
              <button disabled={busy} type="submit">
                Pay Merchant
              </button>
            </form>
          </section>

        </main>
      )}
    </div>
  );
}

export default App;

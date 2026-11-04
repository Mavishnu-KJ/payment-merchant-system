# payment-merchant-system-gui

React + Vite frontend for `payment-merchant-system` backend.

## Features 

- Register and Login flows
- User profile display (`/api/users/me`)
- Wallet details, balance, and add money (`/api/wallets/me`, `/api/wallets/me/balance`, `/api/wallets/me/addMoney`)
- Merchant creation and merchant lookup (`/api/merchants/register`, `/api/merchants/{merchantId}`)
- User transaction history with pagination (`/api/transactions/me/history`)
- Main transaction actions:
  - Pay another user (`/api/transactions/meTransfer`, fallback to `/api/transactions/me/transfer`)
  - Pay merchant (`/api/transactions/me/pay-merchant`)
- Logout support

## Prerequisites

- Node.js 18+
- Running backend API Gateway on `http://localhost:8080`

## Setup

```powershell
cd C:\Users\10861795\IdeaProjects\payment-merchant-system-gui
copy .env.example .env
npm install
npm run dev
```

Open: `http://localhost:5173`

## Build

```powershell
cd C:\Users\10861795\IdeaProjects\payment-merchant-system-gui
npm run build
npm run preview
```

## Notes

- The app stores JWT token in localStorage for session continuity.
- Merchant ID is mapped by `userId` in localStorage because backend contract exposed here does not include a direct `get merchant by current user` endpoint.
- If your gateway path differs, update `VITE_API_BASE_URL` in `.env`.

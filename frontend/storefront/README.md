# Atlas Storefront

Customer-facing e-commerce application built with Next.js 16.

## Features

- Product browsing and search
- Shopping cart
- Checkout process with payment integration (Stripe)
- Order history
- User authentication
- Real-time notifications

## Getting Started

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

The storefront will be available at [http://localhost:8000](http://localhost:8000)

### Build for Production

```bash
npm run build
npm start
```

## Tech Stack

- **Framework**: Next.js 16 (App Router)
- **UI Library**: React 19
- **Styling**: Tailwind CSS 4
- **UI Components**: Radix UI + shadcn/ui
- **State Management**: Zustand
- **Form Handling**: React Hook Form + Zod
- **HTTP Client**: Axios
- **Real-time**: STOMP.js + SockJS
- **Payment**: Stripe

## Folder Structure

```
storefront/
├── src/
│   ├── app/              # Next.js app router pages
│   │   ├── cart/         # Shopping cart page
│   │   ├── checkout/     # Checkout flow
│   │   ├── login/        # Login page
│   │   ├── register/     # Registration page
│   │   └── order-history/# Order history page
│   ├── components/       # React components
│   │   ├── ui/          # Reusable UI components
│   │   ├── layout/      # Layout components (NavBar, etc.)
│   │   ├── front/       # Storefront-specific components
│   │   └── common/      # Common components
│   ├── api/             # API client functions
│   ├── services/        # Business logic services
│   ├── stores/          # Zustand stores
│   ├── hooks/           # Custom React hooks
│   ├── contexts/        # React contexts
│   ├── interfaces/      # TypeScript interfaces
│   ├── constants/       # App constants
│   ├── utils/           # Utility functions
│   └── lib/             # Library configurations
└── public/              # Static assets
```

## Environment Variables

Create a `.env.local` file with:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=your_stripe_key
```

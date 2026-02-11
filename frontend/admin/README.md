# Atlas Admin

Admin dashboard for managing the Atlas e-commerce platform.

## Features

- Admin dashboard with analytics
- Product management (CRUD operations)
- Order management
- User management
- Real-time updates

## Getting Started

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

The admin panel will be available at [http://localhost:8001](http://localhost:8001)

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
- **Charts**: Recharts

## Folder Structure

```
admin/
├── src/
│   ├── app/              # Next.js app router pages
│   │   ├── admin/        # Admin section
│   │   │   ├── dashboard/# Dashboard page
│   │   │   ├── product/  # Product management
│   │   │   ├── order/    # Order management
│   │   │   └── user/     # User management
│   │   ├── login/        # Login page
│   │   └── register/     # Registration page
│   ├── components/       # React components
│   │   ├── ui/          # Reusable UI components
│   │   ├── layout/      # Layout components
│   │   ├── admin/       # Admin-specific components
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
```

## Authentication

Admin users must log in to access the dashboard. The middleware will redirect unauthenticated users to the login page.

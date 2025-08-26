import { NavBar } from "@/components/layout";
import { AuthProvider } from "@/contexts/AuthContext";
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Toaster } from "sonner";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Atlas Store",
  description: "Your one-stop shop for all your needs",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-gradient-to-br from-gray-50 to-gray-100 min-h-screen`}
      >
        <AuthProvider>
          <NavBar />
          <main className="px-10 py-20">{children}</main>
          <Toaster />
        </AuthProvider>
      </body>
    </html>
  );
}

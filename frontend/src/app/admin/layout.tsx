export default function AdminRouteLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return <div className="-mt-20 min-h-screen">{children}</div>;
}

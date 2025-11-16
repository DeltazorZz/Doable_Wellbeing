export default function AuthLayout({ children }: { children: React.ReactNode }) {
return (
<main className="min-h-screen bg-white flex items-center justify-center p-4 text-black">
{children}
</main>
);
}
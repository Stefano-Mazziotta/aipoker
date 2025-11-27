import type { Metadata } from "next";
import { WebSocketProvider } from "@/contexts/WebSocketContext";
import { AuthProvider } from "@/contexts/AuthContext";
import { LobbyProvider } from "@/contexts/LobbyContext";
import { GameProvider } from "@/contexts/GameContext";
import "./globals.css";

export const metadata: Metadata = {
  title: "Texas Hold'em Poker - Online Multiplayer",
  description: "Play Texas Hold'em poker with friends online in real-time",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <WebSocketProvider>
          <AuthProvider>
            <LobbyProvider>
              <GameProvider>
                {children}
              </GameProvider>
            </LobbyProvider>
          </AuthProvider>
        </WebSocketProvider>
      </body>
    </html>
  );
}

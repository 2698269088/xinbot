/*
 * Copyright (C) 2024-2026 huangdihd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package xin.bbtt.mcbot.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.mcbot.events.ServerChangeEvent;

public class ServerRecorder extends SessionAdapter {

    @Override
    public void packetReceived(Session session, Packet packet) {
        Bot bot = Bot.INSTANCE;

        if (session != bot.getSession()) {
            return;
        }

        if (!(packet instanceof ClientboundLoginPacket loginPacket)) {
            return;
        }

        Server oldServer = bot.getServer();
        Server newServer = bot.getPluginManager()
            .getMetaPlugin()
            .getServer(loginPacket);

        if (newServer != null) {
            bot.setServer(newServer);

            bot.getPluginManager()
                .events()
                .callEvent(
                    new ServerChangeEvent(newServer, oldServer)
                );
        }

        // 修复：只要收到登录包就表示登录流程已完成，必须唤醒 Bot.connect() 中等待的线程。
        // 连接普通服务器（如 127.0.0.1）时元插件无法识别出 Login/Game 类型会返回 null，
        // 若此时不唤醒，连接会一直等待到超时并被误断开，导致频繁重连。
        bot.markLoginCompleted(newServer);
    }
}

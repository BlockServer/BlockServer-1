/*
 * This file is part of BlockServer.
 *
 * BlockServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlockServer.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.blockserver.core.message;

import org.blockserver.core.Server;
import org.blockserver.core.events.MessageHandleEvent;
import org.blockserver.core.events.RawPacketHandleEvent;
import org.blockserver.core.modules.network.NetworkConverter;
import org.blockserver.core.modules.network.NetworkModule;
import org.blockserver.core.modules.network.NetworkProvider;
import org.blockserver.core.modules.scheduler.SchedulerModule;

/**
 * Written by Exerosis!
 */
public class MessageModule extends NetworkModule {
    private final NetworkConverter networkConverter;

    public MessageModule(Server server, SchedulerModule schedulerModule, NetworkConverter networkConverter) {
        super(server, schedulerModule);
        this.networkConverter = networkConverter;
        task = () -> {
            for (NetworkProvider provider : getProviders()) {
                provider.receiveInboundPackets().forEach(packet -> {
                    getServer().getEventManager().fire(new RawPacketHandleEvent(packet), event -> {
                        if (!event.isCancelled())
                            getServer().getEventManager().fire(new MessageHandleEvent<>(networkConverter.toMessage(event.getPacket())));
                    });
                });
            }
        };
    }


    public void sendMessage(Message message) {
        for (NetworkProvider provider : getProviders()) {
            provider.queueOutboundPackets(networkConverter.toPacket(message));
        }
    }
}
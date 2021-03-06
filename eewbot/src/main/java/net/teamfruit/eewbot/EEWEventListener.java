package net.teamfruit.eewbot;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import net.teamfruit.eewbot.event.EEWEvent;
import net.teamfruit.eewbot.event.MonitorEvent;
import net.teamfruit.eewbot.event.QuakeInfoEvent;
import net.teamfruit.eewbot.node.EEW;
import net.teamfruit.eewbot.node.QuakeInfo;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class EEWEventListener {

	@EventSubscriber
	public void onEEW(final EEWEvent e) {
		final EEW eew = e.getElement();
		for (final Iterator<Entry<Long, CopyOnWriteArrayList<Channel>>> it1 = EEWBot.instance.getChannels().entrySet().iterator(); it1.hasNext();) {
			final Entry<Long, CopyOnWriteArrayList<Channel>> entry = it1.next();
			for (final Iterator<Channel> it2 = entry.getValue().iterator(); it2.hasNext();) {
				final Channel channel = it2.next();
				if ((eew.isAlert()&&channel.eewAlert.get())||(!eew.isAlert()&&channel.eewPrediction.get())) {
					final Optional<IGuild> guild = Optional.ofNullable(EEWBot.instance.getClient().getGuildByID(entry.getKey()));
					try {
						guild.ifPresent(id -> id.getChannelByID(channel.id).sendMessage(eew.buildEmbed()));
					} catch (final MissingPermissionsException ex) {
						EEWBot.LOGGER.warn("メッセージを送信する権限がありません: "+guild.get().getName()+" #"+guild.get().getChannelByID(channel.id).getName());
					}
				}
			}
		}
	}

	@EventSubscriber
	public void onQuakeInfo(final QuakeInfoEvent e) {
		final QuakeInfo info = e.getElement();
		for (final Iterator<Entry<Long, CopyOnWriteArrayList<Channel>>> it1 = EEWBot.instance.getChannels().entrySet().iterator(); it1.hasNext();) {
			final Entry<Long, CopyOnWriteArrayList<Channel>> entry = it1.next();
			for (final Iterator<Channel> it2 = entry.getValue().iterator(); it2.hasNext();) {
				final Channel channel = it2.next();
				if (channel.quakeInfo.get()) {
					final Optional<IGuild> guild = Optional.ofNullable(EEWBot.instance.getClient().getGuildByID(entry.getKey()));
					try {
						guild.ifPresent(id -> id.getChannelByID(channel.id).sendMessage(info.buildEmbed()));
						if (channel.quakeInfoDetail.get())
							info.getDetails().forEach(detail -> RequestBuffer.request(() -> guild.ifPresent(id -> id.getChannelByID(channel.id).sendMessage(detail.buildEmbed()))));
					} catch (final MissingPermissionsException ex) {
						EEWBot.LOGGER.warn("メッセージを送信する権限がありません: "+guild.get().getName()+" #"+guild.get().getChannelByID(channel.id).getName());
					}
				}
			}
		}
	}

	@EventSubscriber
	public void onMonitor(final MonitorEvent e) {
		for (final Iterator<Entry<Long, CopyOnWriteArrayList<Channel>>> it1 = EEWBot.instance.getChannels().entrySet().iterator(); it1.hasNext();) {
			final Entry<Long, CopyOnWriteArrayList<Channel>> entry = it1.next();
			for (final Iterator<Channel> it2 = entry.getValue().iterator(); it2.hasNext();) {
				final Channel channel = it2.next();
				if (channel.monitor.get()) {
					final IGuild guild = EEWBot.instance.getClient().getGuildByID(entry.getKey());
					if (guild!=null)
						try {
							guild.getChannelByID(channel.id).sendFile("", new ByteArrayInputStream(e.getElement()), "kyoshinmonitor.png");
						} catch (final MissingPermissionsException ex) {
							EEWBot.LOGGER.warn("ファイルを送信する権限がありません: "+guild.getName()+" #"+guild.getChannelByID(channel.id).getName());
						}
				}
			}
		}
	}
}

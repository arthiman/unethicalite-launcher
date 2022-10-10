/*
 * Copyright (c) 2019, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.launcher;

import com.google.common.io.ByteStreams;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.extern.slf4j.Slf4j;

import static net.runelite.launcher.Launcher.*;

@Slf4j
class InfoPanel extends JPanel
{
	private static final Color DARK_GREY = new Color(10, 10, 10, 255);

	private static final BufferedImage TRANSPARENT_LOGO = ImageUtil.getResourceStreamFromClass(InfoPanel.class, "unethicalite.png");
	static final Dimension PANEL_SIZE = new Dimension(200, OpenOSRSSplashScreen.FRAME_SIZE.height);

	private static final Dimension VERSION_SIZE = new Dimension(PANEL_SIZE.width, 25);

	private static final String TROUBLESHOOTING_URL = "https://github.com/unethicalite/unethicalite-launcher/wiki";
	private static final String DISCORD_INVITE_LINK = "https://discord.gg/JBw7KPscwM";
	private static final String LAUNCHER_DOWNLOAD_LINK = "https://unethicalite.net";

	InfoPanel(String mode)
	{
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(PANEL_SIZE);
		this.setBackground(new Color(38, 38, 38));

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 5;

		// Logo
		final ImageIcon transparentLogo = new ImageIcon();
		if (TRANSPARENT_LOGO != null)
		{
			transparentLogo.setImage(TRANSPARENT_LOGO.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
		}
		final JLabel logo = new JLabel(transparentLogo);

		c.anchor = GridBagConstraints.NORTH;
		c.weighty = 1;
		this.add(logo, c);
		c.gridy++;
		c.anchor = GridBagConstraints.SOUTH;
		c.weighty = 0;

		// Latest version
		if (isOutdated())
		{
			this.add(createPanelTextButton("Update available!", () -> LinkBrowser.browse(LAUNCHER_DOWNLOAD_LINK)), c);
			c.gridy++;

			this.add(createPanelTextButton("Latest Version: " + LATEST_VERSION), c);
			c.gridy++;
		}

		// Version
		this.add(createPanelTextButton("Launcher Version: " + LauncherProperties.getVersion()), c);
		c.gridy++;

		// bootstrap
		if (mode != null)
		{
			this.add(createPanelTextButton("Mode: " + mode), c);
			c.gridy++;
		}

		final JLabel logsFolder = createPanelButton("Open logs folder", null, () -> LinkBrowser.openLocalFile(LOGS_DIR));
		this.add(logsFolder, c);
		c.gridy++;

		final JLabel discord = createPanelButton("Join Discord", "Instant invite link to join the Unethicalite discord", () -> LinkBrowser.browse(DISCORD_INVITE_LINK));
		this.add(discord, c);
		c.gridy++;

		final JLabel troubleshooting = createPanelButton("Troubleshooting steps", "Opens a link to the troubleshooting wiki", () -> LinkBrowser.browse(TROUBLESHOOTING_URL));
		this.add(troubleshooting, c);
		c.gridy++;

		final JLabel exit = createPanelButton("Exit", "Closes the application immediately", () -> System.exit(0));
		this.add(exit, c);
		c.gridy++;
	}



	private static JLabel createPanelTextButton(final String title)
	{
		final JLabel textButton = new JLabel(title);
		textButton.setFont(FontManager.getRunescapeSmallFont());
		textButton.setHorizontalAlignment(JLabel.CENTER);
		textButton.setForeground(ColorScheme.BRAND_BLUE);
		textButton.setBackground(null);
		textButton.setPreferredSize(VERSION_SIZE);
		textButton.setMinimumSize(VERSION_SIZE);
		textButton.setBorder(new MatteBorder(1, 0, 0, 0, DARK_GREY));

		return textButton;
	}

	private static JLabel createPanelTextButton(final String title, final Runnable runnable)
	{
		final JLabel textButton = new JLabel(title);
		textButton.setFont(FontManager.getRunescapeSmallFont());
		textButton.setHorizontalAlignment(JLabel.CENTER);
		textButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		textButton.setBackground(null);
		textButton.setPreferredSize(VERSION_SIZE);
		textButton.setMinimumSize(VERSION_SIZE);
		textButton.setBorder(new MatteBorder(1, 0, 0, 0, DARK_GREY));
		textButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		textButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				runnable.run();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				textButton.setBackground(new Color(60, 60, 60));
				textButton.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				textButton.setBackground(null);
				textButton.repaint();
			}
		});

		return textButton;
	}

	private static JLabel createPanelButton(final String name, final String tooltip, final Runnable runnable)
	{
		final JLabel btn = new JLabel(name, JLabel.CENTER);
		btn.setToolTipText(tooltip);
		btn.setOpaque(true);
		btn.setBackground(null);
		btn.setForeground(Color.WHITE);
		btn.setFont(FontManager.getRunescapeFont());
		btn.setBorder(new CompoundBorder(
			new MatteBorder(1, 0, 0, 0, DARK_GREY),
			new EmptyBorder(3, 0, 3, 0))
		);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				runnable.run();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				btn.setBackground(new Color(60, 60, 60));
				btn.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				btn.setBackground(null);
				btn.repaint();
			}
		});

		return btn;
	}
}

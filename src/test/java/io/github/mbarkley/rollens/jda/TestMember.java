package io.github.mbarkley.rollens.jda;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class TestMember implements Member {
  @Setter
  @Getter
  private String nickname;

  @NotNull
  @Override
  public User getUser() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Guild getGuild() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public EnumSet<Permission> getPermissions() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public EnumSet<Permission> getPermissions(@NotNull GuildChannel channel) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public EnumSet<Permission> getPermissionsExplicit() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public EnumSet<Permission> getPermissionsExplicit(@NotNull GuildChannel channel) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPermission(@NotNull Permission... permissions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPermission(@NotNull Collection<Permission> permissions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPermission(@NotNull GuildChannel channel, @NotNull Permission... permissions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPermission(@NotNull GuildChannel channel, @NotNull Collection<Permission> permissions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canSync(@NotNull GuildChannel targetChannel, @NotNull GuildChannel syncSource) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canSync(@NotNull GuildChannel channel) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public JDA getJDA() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public OffsetDateTime getTimeJoined() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasTimeJoined() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public OffsetDateTime getTimeBoosted() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public GuildVoiceState getVoiceState() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<Activity> getActivities() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public OnlineStatus getOnlineStatus() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public OnlineStatus getOnlineStatus(@NotNull ClientType type) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public EnumSet<ClientType> getActiveClients() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public String getEffectiveName() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<Role> getRoles() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public Color getColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getColorRaw() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canInteract(@NotNull Member member) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canInteract(@NotNull Role role) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canInteract(@NotNull Emote emote) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isOwner() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public TextChannel getDefaultChannel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFake() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public String getAsMention() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getIdLong() {
    throw new UnsupportedOperationException();
  }
}

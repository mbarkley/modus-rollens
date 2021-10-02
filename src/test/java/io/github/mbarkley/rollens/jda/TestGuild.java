package io.github.mbarkley.rollens.jda;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.templates.Template;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TestGuild implements Guild {
  private final long id;

  @NotNull
  @Override
  public RestAction<List<Command>> retrieveCommands() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Command> retrieveCommandById(@NotNull String s) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public CommandCreateAction upsertCommand(@NotNull CommandData commandData) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public CommandListUpdateAction updateCommands() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public CommandEditAction editCommandById(@NotNull String s) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Void> deleteCommandById(@NotNull String s) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<CommandPrivilege>> retrieveCommandPrivilegesById(@NotNull String s) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Map<String, List<CommandPrivilege>>> retrieveCommandPrivileges() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(@NotNull String s, @NotNull Collection<? extends CommandPrivilege> collection) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Map<String, List<CommandPrivilege>>> updateCommandPrivileges(@NotNull Map<String, Collection<? extends CommandPrivilege>> map) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public MemberAction addMember(@NotNull String accessToken, @NotNull String userId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLoaded() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void pruneMemberCache() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean unloadMember(long userId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMemberCount() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public String getIconId() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Set<String> getFeatures() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public String getSplashId() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<String> retrieveVanityUrl() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public String getVanityCode() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<VanityInvite> retrieveVanityInvite() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public String getDescription() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Locale getLocale() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public String getBannerId() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public BoostTier getBoostTier() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getBoostCount() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<Member> getBoosters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMaxMembers() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMaxPresences() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<MetaData> retrieveMetaData() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public VoiceChannel getAfkChannel() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public TextChannel getSystemChannel() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public TextChannel getRulesChannel() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public TextChannel getCommunityUpdatesChannel() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public Member getOwner() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getOwnerIdLong() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Timeout getAfkTimeout() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public String getRegionRaw() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMember(@NotNull User user) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Member getSelfMember() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public NSFWLevel getNSFWLevel() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public Member getMember(@NotNull User user) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public MemberCacheView getMemberCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SortedSnowflakeCacheView<Category> getCategoryCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SortedSnowflakeCacheView<StoreChannel> getStoreChannelCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SortedSnowflakeCacheView<TextChannel> getTextChannelCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<GuildChannel> getChannels(boolean includeHidden) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SortedSnowflakeCacheView<Role> getRoleCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SnowflakeCacheView<Emote> getEmoteCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<ListedEmote>> retrieveEmotes() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<ListedEmote> retrieveEmoteById(@NotNull String id) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<Ban>> retrieveBanList() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Ban> retrieveBanById(@NotNull String userId) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Integer> retrievePrunableMemberCount(int days) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Role getPublicRole() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public TextChannel getDefaultChannel() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public GuildManager getManager() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditLogPaginationAction retrieveAuditLogs() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Void> leave() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Void> delete() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Void> delete(@Nullable String mfaCode) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AudioManager getAudioManager() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Task<Void> requestToSpeak() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Task<Void> cancelRequestToSpeak() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public JDA getJDA() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<Invite>> retrieveInvites() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<Template>> retrieveTemplates() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Template> createTemplate(@NotNull String s, @Nullable String s1) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<List<Webhook>> retrieveWebhooks() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<GuildVoiceState> getVoiceStates() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public VerificationLevel getVerificationLevel() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public NotificationLevel getDefaultNotificationLevel() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public MFALevel getRequiredMFALevel() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ExplicitContentLevel getExplicitContentLevel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean checkVerification() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAvailable() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public CompletableFuture<Void> retrieveMembers() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Task<Void> loadMembers(@NotNull Consumer<Member> callback) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Member> retrieveMemberById(long id, boolean update) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Task<List<Member>> retrieveMembersByIds(boolean includePresence, @NotNull long... ids) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Task<List<Member>> retrieveMembersByPrefix(@NotNull String prefix, int limit) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RestAction<Void> moveVoiceMember(@NotNull Member member, @Nullable VoiceChannel voiceChannel) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> modifyNickname(@NotNull Member member, @Nullable String nickname) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Integer> prune(int days, boolean wait, @NotNull Role... roles) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> kick(@NotNull Member member, @Nullable String reason) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> kick(@NotNull String userId, @Nullable String reason) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> ban(@NotNull User user, int delDays, @Nullable String reason) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> ban(@NotNull String userId, int delDays, @Nullable String reason) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> unban(@NotNull String userId) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> deafen(@NotNull Member member, boolean deafen) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> mute(@NotNull Member member, boolean mute) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> addRoleToMember(@NotNull Member member, @NotNull Role role) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> removeRoleFromMember(@NotNull Member member, @NotNull Role role) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> modifyMemberRoles(@NotNull Member member, @Nullable Collection<Role> rolesToAdd, @Nullable Collection<Role> rolesToRemove) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> modifyMemberRoles(@NotNull Member member, @NotNull Collection<Role> roles) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Void> transferOwnership(@NotNull Member newOwner) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelAction<TextChannel> createTextChannel(@NotNull String name, @Nullable Category parent) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelAction<VoiceChannel> createVoiceChannel(@NotNull String name, @Nullable Category parent) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelAction<StageChannel> createStageChannel(@NotNull String s, @Nullable Category category) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelAction<Category> createCategory(@NotNull String name) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RoleAction createRole() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public AuditableRestAction<Emote> createEmote(@NotNull String name, @NotNull Icon icon, @NotNull Role... roles) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelOrderAction modifyCategoryPositions() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelOrderAction modifyTextChannelPositions() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ChannelOrderAction modifyVoiceChannelPositions() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public CategoryOrderAction modifyTextChannelPositions(@NotNull Category category) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public CategoryOrderAction modifyVoiceChannelPositions(@NotNull Category category) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public RoleOrderAction modifyRolePositions(boolean useAscendingOrder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getIdLong() {
    return id;
  }
}

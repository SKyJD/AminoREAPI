package com.mrpowergamerbr.aminoreapi.entities

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.mrpowergamerbr.aminoreapi.Amino
import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.aminoreapi._println
import com.mrpowergamerbr.aminoreapi.utils.Endpoints

data class AminoCommunity(
		var aminoClient: AminoClient,
		var id: String,
		var searchable: Boolean = false,
		@SerializedName("agent")
		var leader: AminoUser, // Seems to be leader
		var listedStatus: Int,
		var probationStatus: Int,
		var keywords: String,
		var themePack: AminoThemePack,
		val membersCount: Int,
		val primaryLanguage: String,
		val communityHeat: String,
		val mediaList: String,
		val content: String,
		val tagline: String,
		val advancedSettings: AminoCommunityAdvancedSettings,
		val joinType: Int,
		val status: Int,
		val modifiedTime: String,
		val ndcId: Int,
		val link: String,
		val icon: String,
		val endpoint: String,
		val name: String,
		val extensions: String,
		val templateId: Int,
		val createdTime: String) {

	fun join() {
		var response = HttpRequest
				.post(String.format(Endpoints.JOIN_COMMUNITY, id))
				.header("NDCAUTH", "sid=" + aminoClient.sid)
				.acceptJson()
				.body();

		println(response)
	}

	fun join(invitationUrl: String) {
		// First we are going to get the invitation ID
		var invitationId = aminoClient.getInvitationInfo(invitationUrl).invitationId

		val innerObject = JsonObject()

		innerObject.addProperty("invitationId", invitationId);

		// Then we are going to send a join community request with the invitationId
		var response = HttpRequest
				.post(String.format(Endpoints.JOIN_COMMUNITY, id))
				.header("NDCAUTH", "sid=" + aminoClient.sid)
				.acceptJson()
				.send(innerObject.toString())
				.body();

		println(response)
	}

	fun leave() {
		var response = HttpRequest
				.post(String.format(Endpoints.LEAVE_COMMUNITY, id))
				.header("NDCAUTH", "sid=" + aminoClient.sid)
				.acceptJson()
				.body();

		println(response)
	}

	fun getBlogFeed(start: Int, size: Int): List<AminoBlogPost> {
		var response = HttpRequest
				.get(String.format(Endpoints.COMMUNITY_FEED, id, start, size))
				.header("NDCAUTH", "sid=" + aminoClient.sid)
				.acceptJson()
				.body();

		println(response);

		var parser = JsonParser();
		var parsedJson = parser.parse(response).asJsonObject.get("blogList").asJsonArray;

		var blogFeed = Amino.gson.fromJson<List<AminoBlogPost>>(parsedJson)

		for (post in blogFeed) {
			post.community = this;
		}
		return blogFeed;
	}

	fun getMemberCount(): AminoMemberCount {
		var response = HttpRequest
				.get(String.format(Endpoints.COMMUNITY_ONLINE_MEMBERS, id))
				.header("NDCAUTH", "sid=" + aminoClient.sid)
				.acceptJson()
				.body();

		_println(response)

		var parser = JsonParser();
		var parsedJson = parser.parse(response).asJsonObject.get("onlineMembersCheckResult").asJsonArray;

		var memberCount = Amino.gson.fromJson(parsedJson, AminoMemberCount::class.java)

		return memberCount;
	}


	fun getAllChats(chatType: String, start: Int, size: Int, cv: Double): List<AminoChatThread> {
		var response = HttpRequest
				.get(String.format(Endpoints.COMMUNITY_CHAT_THREAD, id, chatType, start, size, cv))
				.header("NDCAUTH", "sid=" + aminoClient.sid)
				.acceptJson()
				.body();

		_println(response)

		var parser = JsonParser();
		var parsedJson = parser.parse(response).asJsonObject.get("threadList").asJsonArray;

		var chatThreads = Amino.gson.fromJson<List<AminoChatThread>>(parsedJson)

		for (thread in chatThreads) {
			thread.community = this;
		}

		return chatThreads;
	}
}

data class AminoUser(
		val status: Int,
		val uid: String,
		val level: String,
		val mood: String,
		val reputation: Int,
		val role: Int,
		val nickname: String,
		val icon: String)

data class AminoThemePack(
		val themeColor: String,
		val themePackHash: String,
		val themePackRevision: Int,
		val themePackUrl: String)

data class AminoCommunityAdvancedSettings(
		val defaultRankingTypeInLeaderboard: Int,
		val frontPageLayout: Int,
		val hasPendingReviewRequest: Boolean,
		val welcomeMessageEnabled: String,
		val welcomeMessageText: String,
		val pollMinFullBarVoteCount: Int,
		val catalogEnabled: Boolean)

data class AminoInvitation(
		val status: Int,
		val duration: Int,
		val invitationId: String,
		val link: String,
		val modifiedTime: String,
		val ndcId: Int,
		val createdTime: String,
		val inviteCode: String)

data class AminoBlogPost(
		var community: AminoCommunity,
		val status: Int,
		val style: Int,
		val modifiedTime: String,
		val title: String,
		val author: AminoUser,
		val contentRating: Int,
		val votedValue: Int,
		val content: String,
		val keywords: String,
		val latitude: Int,
		val longitude: Int,
		val endTime: String,
		val type: Int,
		val blogId: String,
		val commentsCount: Int,
		val mediaList: List<Object>) {

	fun like(cv: Double, value: Int) {
		var response = HttpRequest
				.post(String.format(Endpoints.POST_VOTE + "?cv=%s&value=%s", community.id, blogId, cv, value))
				.header("NDCAUTH", "sid=" + community.aminoClient.sid)
				.acceptJson()
				.body();

		_println(response)
	}

	fun unlike() {
		var response = HttpRequest
				.delete(String.format(Endpoints.POST_VOTE, community.id, blogId))
				.header("NDCAUTH", "sid=" + community.aminoClient.sid)
				.acceptJson()
				.body();

		_println(response);
	}

	fun comment(message: String) {
		val innerObject = JsonObject()

		innerObject.addProperty("content", message)
		innerObject.add("mediaList", JsonArray())

		var response = HttpRequest
				.post(String.format(Endpoints.POST_COMMENT, community.id, blogId))
				.header("NDCAUTH", "sid=" + community.aminoClient.sid)
				.acceptJson()
				.send(innerObject.toString())
				.body();

		_println(response);
	}
}

data class AminoMemberCount(
		val onlineMembersCount: Int,
		val otherOnlineMembersCount: Int
)

data class AminoChatThread(
		var community: AminoCommunity,
		val uid: String,
		val membersQuota: Int,
		val membersSummary: List<AminoUser>,
		val threadId: String,
		val keywords: String,
		val membersCount: Int,
		val title: String,
		val membershipStatus: Int,
		val content: String,
		val latitude: Int,
		val longitude: Int,
		val alertOption: Int,
		val lastReadTime: String,
		val type: Int,
		val status: Int,
		val modifiedTime: String,
		val condition: Int,
		val icon: String,
		val latestActivityTime: String,
		val extensions: Object,
		val createdTime: String) {

	fun join() {
		var response = HttpRequest
				.post(String.format(Endpoints.COMMUNITY_JOIN_CHAT_THREAD, community.id, threadId, community.aminoClient.uid))
				.header("NDCAUTH", "sid=" + community.aminoClient.sid)
				.acceptJson()
				.body();

		_println(response);
	}

	fun sendMessage(chatMessage: String) {
		if (!isMember()) { // If we aren't a member of this chat, then we need to join it!
			join();
		}
		val innerObject = JsonObject()

		innerObject.addProperty("content", chatMessage)
		innerObject.addProperty("type", 0)
		innerObject.addProperty("clientRefId", 843397539)

		var response = HttpRequest
				.post(String.format(Endpoints.COMMUNITY_CHAT_SEND_MESSAGE, community.id, threadId, chatMessage))
				.header("NDCAUTH", "sid=" + community.aminoClient.sid)
				.acceptJson()
				.send(innerObject.toString())
				.body();

		_println(response)
	}

	fun isMember(): Boolean {
		return membershipStatus > 0;
	}
}

data class AminoMessage(
		val community: AminoCommunity,
		val uid: String,
		val mediaType: Int,
		val content: String,
		val messageId: String,
		val createdTime: String,
		val type: Int,
		val mediaValue: List<Object>)

data class AminoExtensions(
		val bm: List<String>,
		val bannedMemberUidList: List<String>)
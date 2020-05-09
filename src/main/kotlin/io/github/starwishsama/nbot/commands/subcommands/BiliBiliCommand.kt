package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BiliBiliUtil
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.isNumeric
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage

class BiliBiliCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)) {
            if (args.isEmpty()) {
                return getHelp().toMirai()
            } else {
                when (args[0]) {
                    "sub", "订阅" -> {
                        if (args.size > 1) {
                            if (user.isBotAdmin()) {
                                val mid: Long
                                mid = if (args[1].isNumeric()) {
                                    args[1].toLong()
                                } else {
                                    val item = BiliBiliUtil.getUser(args[1])
                                    item?.roomid ?: return BotUtil.sendMsgPrefix("账号不存在").toMirai()
                                }
                                BotConstants.cfg.subList.add(mid)
                                return BotUtil.sendMsgPrefix("订阅 ${BiliBiliUtil.getUserNameByMid(mid)} 的直播间成功").toMirai()
                            } else {
                                BotUtil.sendMsgPrefix("你没有权限").toMirai()
                            }
                        } else {
                            return getHelp().toMirai()
                        }
                    }
                    "unsub", "取消订阅" -> {
                        if (args.size > 1) {
                            var roomId = 0L
                            if (args[1].isNumeric()) {
                                roomId = args[1].toLong()
                            } else {
                                val item = BiliBiliUtil.getUser(args[1])
                                if (item != null) {
                                    roomId = item.mid
                                }
                            }

                            return if (!BotConstants.cfg.subList.contains(roomId)) {
                                BotUtil.sendMsgPrefix("你还没订阅直播间 ${args[1]}").toMirai()
                            } else {
                                BotConstants.cfg.subList.remove(args[1].toLong())
                                BotUtil.sendMsgPrefix("取消订阅直播间 ${args[1]} 成功").toMirai()
                            }
                        } else {
                            getHelp().toMirai()
                        }
                    }
                    "list" -> {
                        val subs = StringBuilder("监控室列表:\n")
                        BotConstants.cfg.subList.forEach {
                            val room = BiliBiliUtil.getLiveRoom(it)
                            subs.append("${BiliBiliUtil.getUserNameByMid(room.data.uid)} ${if (room.data.liveStatus == 1) "✔" else "✘"}\n")
                        }
                        return subs.toString().trim().toMirai()
                    }
                    "info", "查询" -> {
                        event.quoteReply("请稍等...")
                        val item = BiliBiliUtil.getUser(args[1])
                        return if (item != null) {
                            val before = item.title + "\n粉丝数: " + item.fans +
                                    "\n最近视频: " + (if (!item.avItems.isNullOrEmpty()) item.avItems[0].title else "没有投稿过视频") +
                                    "\n直播状态: " + (if (item.liveStatus == 1) "✔" else "✘")
                            val dynamic = BiliBiliUtil.getDynamic(item.mid)
                            before.toMirai() + getDynamicText(dynamic, event)
                        } else {
                            BotUtil.sendMsgPrefix("账号不存在").toMirai()
                        }

                    }
                    else -> return getHelp().toMirai()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("bili", arrayListOf(), "订阅B站主播/查询用户动态", "nbot.commands.bili", UserLevel.USER)

    override fun getHelp(): String = """
        /bili sub [用户名] 订阅用户相关信息
        /bili unsub [用户名] 取消订阅用户相关信息
        /bili info [用户名] 查看用户的动态
    """.trimIndent()

    private suspend fun getDynamicText(dynamic: List<String>, event: MessageEvent): MessageChain {
        return if (dynamic.isEmpty()) {
            ("\n无最近动态").toMirai()
        } else {
            when (dynamic.size) {
                1 -> ("\n最近动态: ${dynamic[0]}").toMirai()
                2 -> ("\n最近动态: ${dynamic[0]}").toMirai()
                        .plus(BotUtil.getImageStream(dynamic[1]).uploadAsImage(event.subject))
                else -> ("\n无最近动态").toMirai()
            }
        }
    }

}
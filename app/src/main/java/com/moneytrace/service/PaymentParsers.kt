package com.moneytrace.service

/**
 * 解析出的支付信息
 */
data class ParsedPayment(
    val amount: Double,
    val merchant: String,
    val source: String,     // ALIPAY / WECHAT
    val type: String = "EXPENSE"
)

/**
 * 通知解析接口
 */
interface NotificationParser {
    fun parse(packageName: String, title: String, content: String): ParsedPayment?
}

/**
 * 支付宝通知解析器
 * 支持格式：
 * - "您已成功付款¥38.50，收款方：美团外卖"
 * - "支付宝收款¥100.00"
 * - "转账给XXX¥50.00"
 */
class AlipayParser : NotificationParser {
    private val amountRegex = Regex("""[¥￥](\d+(?:\.\d{1,2})?)""")
    private val merchantPatterns = listOf(
        Regex("""收款方[：:](.+?)(?:[，,。\n]|$)"""),
        Regex("""付款给(.+?)(?:\s|[，,。¥￥\n]|$)"""),
        Regex("""向(.+?)转账""")
    )

    override fun parse(packageName: String, title: String, content: String): ParsedPayment? {
        if (packageName != "com.eg.android.AlipayGphone") return null

        // 只处理支出相关通知，过滤收款、营销消息
        val isExpense = content.contains("付款") || content.contains("支出") ||
                        content.contains("转账给") || content.contains("消费")
        val isIgnore = content.contains("收到") && !content.contains("付款") ||
                       content.contains("红包") || content.contains("余额宝") ||
                       content.contains("花呗账单") || title.contains("花呗") && !content.contains("付款")
        if (!isExpense || isIgnore) return null

        val amount = amountRegex.find(content)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: return null
        if (amount <= 0.01) return null

        val merchant = merchantPatterns.firstNotNullOfOrNull { regex ->
            regex.find(content)?.groupValues?.get(1)?.trim()?.take(30)
        } ?: "支付宝支付"

        return ParsedPayment(
            amount = amount,
            merchant = cleanMerchantName(merchant),
            source = "ALIPAY"
        )
    }

    private fun cleanMerchantName(name: String): String {
        return name.replace(Regex("""[^\u4e00-\u9fa5a-zA-Z0-9·（）()·\-]"""), "").trim()
            .ifBlank { "支付宝支付" }
    }
}

/**
 * 微信支付通知解析器
 * 支持格式：
 * - "你已付款给XX餐厅¥28.00"
 * - "[微信支付]微信支付成功¥156.80"
 */
class WeChatPayParser : NotificationParser {
    private val amountRegex = Regex("""[¥￥](\d+(?:\.\d{1,2})?)""")
    private val merchantPatterns = listOf(
        Regex("""付款给(.+?)[¥￥\s，,]"""),
        Regex("""向(.+?)(?:付款|转账)[¥￥]"""),
        Regex("""[（(](.+?)[)）]""")
    )

    override fun parse(packageName: String, title: String, content: String): ParsedPayment? {
        val isWechatPay = packageName == "com.tencent.mm" &&
                (title.contains("微信支付") || title.contains("支付成功") ||
                 content.contains("付款") && content.contains("¥"))
        if (!isWechatPay) return null

        // 过滤收款、红包等
        if (content.contains("收款成功") || content.contains("转入") ||
            content.contains("红包") || content.contains("退款")) return null

        val amount = amountRegex.find(content)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: return null
        if (amount <= 0.01) return null

        val merchant = merchantPatterns.firstNotNullOfOrNull { regex ->
            regex.find(content)?.groupValues?.get(1)?.trim()?.take(30)
        } ?: "微信支付"

        return ParsedPayment(
            amount = amount,
            merchant = merchant.ifBlank { "微信支付" },
            source = "WECHAT"
        )
    }
}

/**
 * 银行短信解析器（通过通知栏短信）
 * 格式：招商银行尾号XXXX消费¥XX.XX
 */
class BankSmsParser : NotificationParser {
    private val bankPackages = setOf(
        "com.android.mms", "com.google.android.apps.messaging",
        "com.miui.sms", "com.samsung.android.messaging"
    )
    private val expenseRegex = Regex("""消费[人民币￥¥]?(\d+(?:\.\d{1,2})?)""")
    private val merchantRegex = Regex("""在(.+?)(?:消费|刷卡)""")

    override fun parse(packageName: String, title: String, content: String): ParsedPayment? {
        if (packageName !in bankPackages) return null
        // 匹配银行消费提醒关键词
        if (!content.contains("消费") || !content.contains("尾号")) return null

        val amount = expenseRegex.find(content)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: return null
        if (amount <= 0.01) return null

        val merchant = merchantRegex.find(content)?.groupValues?.get(1)?.trim() ?: "银行卡消费"
        val bankName = when {
            title.contains("招商") || content.contains("招商") -> "招商银行"
            title.contains("工行") || content.contains("工商银行") -> "工商银行"
            title.contains("建行") || content.contains("建设银行") -> "建设银行"
            title.contains("农行") || content.contains("农业银行") -> "农业银行"
            title.contains("中行") || content.contains("中国银行") -> "中国银行"
            else -> "银行卡"
        }

        return ParsedPayment(
            amount = amount,
            merchant = "$bankName·$merchant",
            source = "SMS"
        )
    }
}

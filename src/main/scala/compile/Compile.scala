package compile

object Compile {
	def convert(code: String): String = {
		code.replace("&amp;", "&").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "'")
	}
}
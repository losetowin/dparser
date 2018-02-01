package dparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.dutycode.dparser.htmlparser.HtmlParser;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class HtmlParserTest {

	@Test
	public void testParser(){
		HtmlParser.transferHtml("dsad", HtmlParser.class);
	};


	@Test
	public void testClazz(){
		System.out.println(HtmlParser.class.getCanonicalName());
	}




	@Test
	public void testHtml(){

		String url = "http://stock.finance.sina.com.cn/hkstock/history/01021.html";
		try {
			Document doc = Jsoup.parse(new URL(url), 20000);


			HtmlParser.initHtmlParserConfig("/Users/zzh/Documents/IntelljJWorkspace/dparser/src/test/java/dparser/demoparser.xml");


			List<HkStockData> list = HtmlParser.transferHtmlList(doc, HkStockData.class, "hkstock_list", "0");

			System.out.println(list);


			System.out.println(doc.html());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}




}

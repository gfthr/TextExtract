/*
 * TextExtract.java
 *
 * Copyright (c) 2014 Chengdu Lanjing Data&Information Co., Ltd
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TextExtract {

	private List<String> lines;
	private final static int blocksWidth=5;
	private int threshold;
	private String html;
	private boolean flag;
	private int start;
	private int end;
	private StringBuilder text;
	private ArrayList<Integer> indexDistribution;

	public TextExtract() {
		lines = new ArrayList<String>();
		indexDistribution = new ArrayList<Integer>();
		text = new StringBuilder();
		flag = false;
		/* 当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。*/
		/* 阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文 */
		/*但可以考虑机器学习的方法根据输入文本的某些特征动态地调整此阈值*/
		threshold	= -1;
	}



	/**
	 * 抽取网页正文，不判断该网页是否是目录型。即已知传入的肯定是可以抽取正文的主题类网页。
	 * 
	 * @param _html 网页HTML字符串
	 * 
	 * @return 网页正文string
	 */
	public String parse(String _html) throws IOException {
		return parse(_html, false);
	}
	
	/**
	 * 判断传入HTML，若是主题类网页，则抽取正文；否则输出<b>"unkown"</b>。
	 * 
	 * @param _html 网页HTML字符串
	 * @param _flag true进行主题类判断, 省略此参数则默认为false
	 * 
	 * @return 网页正文string
	 */
	public String parse(String _html, boolean _flag) throws IOException {
		flag = _flag;
		html = _html;
		html = preProcess(html);
//		System.out.println(html);
		return getText();
	}
	private static int FREQUENT_URL = 30;
	private static Pattern links = Pattern.compile("<[aA]\\s+[Hh][Rr][Ee][Ff]=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</a>(\\s*.{0,"+FREQUENT_URL+"}\\s*<a\\s+href=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</[aA]>){2,100}", Pattern.DOTALL);
	private static String preProcess(String source) {



		source = source.replaceAll("(?is)<!DOCTYPE.*?>", "");
		source = source.replaceAll("(?is)<!--.*?-->", "");				// remove html comment
		source = source.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
		source = source.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
		source = source.replaceAll("&.{2,5};|&#.{2,5};", " ");			// remove special char
		
		//剔除连续成片的超链接文本（认为是，广告或噪音）,超链接多藏于span中
		source = source.replaceAll("<[sS][pP][aA][nN].*?>", "");
		source = source.replaceAll("</[sS][pP][aA][nN]>", "");


		int len = source.length();
		//将所有链接替换为空字符
//		while ((source = links.matcher(source).replaceAll("")).length() != len)
//		{
//			len = source.length();
//		}
			;//continue;
		
		//source = links.matcher(source).replaceAll("");
		
		//防止html中在<>中包括大于号的判断
		//source = source.replaceAll("<[^>'\"]*['\"].*['\"].*?>","");
		source = source.replaceAll("\r\n", "\n");
		source = source.replaceAll("<[^>]*>", "");

		return source;
	
	}

	private String getText()  {
//		html = "p style= margin: 19.5pt 0cm; text-indent: 24pt; line-height: 18pt; background-repeat: initial initial;      strong  span   问题一：你对周其仁与华生关于土地制度的争论怎么评价？ /span  /strong  span lang= EN-US    style= mso-bidi-font-size:10.5pt;color:#252525;      /span  /p \n" +
//				"\n" +
//				" p style= margin: 19.5pt 0cm; text-indent: 24pt; line-height: 18pt; background-repeat: initial initial;      strong  span   秦晖：华生是反对土地私有制的 /span  /strong  span style= mso-bidi-font-size: 10.5pt;font-family:宋体;mso-ascii-font-family:Calibri;mso-ascii-theme-font:minor-latin; mso-fareast-font-family:宋体;mso-fareast-theme-font:minor-fareast;mso-hansi-font-family: Calibri;mso-hansi-theme-font:minor-latin;color:#252525;     ，但他反对的理由和现在流行的那种说法不一样。流行的说法是，土地一私有就会出现土地兼并和无地农民，还有可能爆发农民战争。 strong 华生一直强调 /strong 的其实不是这些东西，而 strong 是所谓的土地规划问题。 /strong 他一再讲， strong 他是不反对土地私有制的，只是主张政府要有规划权。 /strong 如果华生真的是这么想的，我觉得他和周其仁之间就不应该有什么分歧。实际上，华生还是要为政府的征地权辩护的。对华生而言，我觉得最值得提出的一个问题就是，政府的规划需要用强制征地这种办法来实现吗？华生说政府对土地用途的管制是全世界一个普遍现象，但像中国这种强制征地可不是全世界普遍的现象，这两者之间的逻辑是可以互相推演出来的吗？当然不是的。 /span  b  span style= mso-bidi-font-size:10.5pt; font-family:宋体;mso-ascii-font-family:Calibri;mso-ascii-theme-font:minor-latin; mso-fareast-font-family:宋体;mso-fareast-theme-font:minor-fareast;mso-hansi-font-family: Calibri;mso-hansi-theme-font:minor-latin;color:red;     参考阅读： /span  /b  b  span style= mso-bidi-font-size:10.5pt; color:red;       /span  /b  b  span style= mso-bidi-font-size:10.5pt;font-family:宋体;mso-ascii-font-family:Calibri; mso-ascii-theme-font:minor-latin;mso-fareast-font-family:宋体;mso-fareast-theme-font: minor-fareast;mso-hansi-font-family:Calibri;mso-hansi-theme-font:minor-latin; color:red;     华生著《城市化转型与土地陷阱》 /span  /b  b  span lang= EN-US    style= mso-bidi-font-size:10.5pt;color:#252525;      /span  /b  /p  div  img title= 秦晖：政府会不会动农民的财产？ - 东方时政观察 - 东方时政观察    alt= 秦晖：政府会不会动农民的财产？ - 东方时政观察 - 东方时政观察    style= margin:0 10px 0 0;    src= http://imgcdn.ph.126.net/X1TQl1GgRM40oZDlDr_lRQ==/998954692364338651.jpg      /div  p style= margin: 19.5pt 0cm; text-indent: 24pt; line-height: 18pt; background-repeat: initial initial;      nbsp; /p \n" +
//				"\n" +
//				" p style= margin: 19.5pt 0cm; text-indent: 24pt; line-height: 18pt; background-repeat: initial initial;      strong  span   问题二：历史上，中国的地权到底姓公还是姓私？ /span  /strong  span lang= EN-US    style= mso-bidi-font-size:10.5pt;color:#252525;      /span  /p ";



		lines = Arrays.asList(html.split("\n")); //将预处理后的网页拆分成行

		indexDistribution.clear();
		
		int empty = 0;//空行的数量
		for (int i = 0; i < lines.size() - blocksWidth; i++) {
			
			if (lines.get(i).length() == 0)
			{
				empty++;
			}
			
			int wordsNum = 0;
			for (int j = i; j < i + blocksWidth; j++) { 
				lines.set(j, lines.get(j).replaceAll("\\s+", "")); //消除空白，却不知为何不是\s+
				wordsNum += lines.get(j).length(); //统计消除空白后的单词数
			}
			//词数目太低则计作零
			indexDistribution.add(wordsNum<50?0:wordsNum);//存储i后面blocksWidth行的单词数量
			//System.out.println(wordsNum);
		}


		int sum = 0;

		for (int i=0; i< indexDistribution.size(); i++)
		{
			sum += indexDistribution.get(i);  //用于计算平均每块的单词数
		}

		int mean=sum/indexDistribution.size();

		/*主题型网页判断*/
		//计算零范数(稀疏度%)
		int L0 = 0;
		for (int i=0; i< indexDistribution.size(); i++)
		{
			L0 += indexDistribution.get(i)>0?100:0;  //用于计算平均每块的单词数
		}

		L0=L0/indexDistribution.size();
/*		try {
			if (L0 < 3);
		}
		catch (Exception e){

		}*/

		if (L0<3) {
			System.out.println("该网页不是主题型的，无法提取正文");
			return null ;
		}

		threshold = Math.min(100, (sum/indexDistribution.size())<<(empty/(lines.size()-empty)>>>1)); //>>>同>>，有空行则
		//将阈值升高
		threshold = Math.max(50, threshold);

		threshold=((int) Math.sqrt(L0))*threshold;//可以调整

		start = -1; end = -1;
		boolean boolstart = false, boolend = false;
//		boolean firstMatch = true;//标志是否为标题的一个flag。前面的标题块往往比较小，应该减小与它匹配的阈值
		text.setLength(0);
		
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < indexDistribution.size() - 1; i++) {
			
//			if(firstMatch)
//			{
//				if (indexDistribution.get(i) > (threshold/2) && ! boolstart) {  //对于标题（一般在前面）取threshold/2
//					if (indexDistribution.get(i+1).intValue() != 0
//						|| indexDistribution.get(i+2).intValue() != 0) {//
//						firstMatch = false;
//						boolstart = true;
//						start = i;
//						continue;
//					}
//				}
//
//			}
			if (indexDistribution.get(i) > threshold && ! boolstart) {
				if (indexDistribution.get(i+1).intValue() != 0 
					|| indexDistribution.get(i+2).intValue() != 0
					|| indexDistribution.get(i+3).intValue() != 0) {
					boolstart = true;
					start = i;
					continue;
				}
			}
			if (boolstart) {
			/*	if (indexDistribution.get(i).intValue() == 0
					|| indexDistribution.get(i+1).intValue() == 0) {
					end = i;
					boolend = true;
				}*/
				if (indexDistribution.get(i).intValue() <= threshold/2
						|| indexDistribution.get(i+1).intValue() <= threshold/3) {
					end = i;
					boolend = true;
				}
			}
		
			if (boolend) {
				buffer.setLength(0);
				//System.out.println(start+1 + "\t\t" + end+1);
				for (int ii = start; ii <= end; ii++) {
					if (lines.get(ii).length() < 5) continue; //字符数小于5的行认为不是正文（还是按行而不是行块抽取的）
					buffer.append(lines.get(ii) + "\n");
				}
				String str = buffer.toString();
				//System.out.println(str);
				if (str.contains("Copyright")  || str.contains("版权所有") ) continue; 
				text.append(str);
				boolstart = boolend = false;
			}
		}
		
		if (start > end)  //对应于文档最后面全是正“正文”
		{
			buffer.setLength(0);
			int size_1 = lines.size()-1;
			for (int ii = start; ii <= size_1; ii++) {
				if (lines.get(ii).length() < 5) continue;
				buffer.append(lines.get(ii) + "\n");
			}
			String str = buffer.toString();
			//System.out.println(str);
			if ((!str.contains("Copyright"))  || (!str.contains("版权所有")) ) 
			{	
				text.append(str);
			}
		}
		
		return text.toString();
	}
	
	public static void main(String[] args)  {
		System.out.println("===============");
		String s = "<img  class='fit-image' onload='javascript:if(this.width>498)this.width=498;' />hello";
		//source = source.replaceAll("<[^'\"]*['\"].*['\"].*?>", "");
        System.out.println(TextExtract.preProcess(s));
	}
}
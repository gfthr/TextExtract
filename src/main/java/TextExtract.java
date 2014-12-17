/*
 * TextExtract.java
 *
 * Copyright (c) 2014 Chengdu Lanjing Data&Information Co., Ltd
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	private static String preProcess(String source) {

		source = source.replaceAll("(?is)<!DOCTYPE.*?>", "");
		source = source.replaceAll("(?is)<!--.*?-->", "");				// remove html comment
		source = source.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
		source = source.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
		source = source.replaceAll("&.{2,5};|&#.{2,5};", " ");			// remove special char
		
		//剔除连续成片的超链接文本（认为是，广告或噪音）,超链接多藏于span中
		source = source.replaceAll("<[sS][pP][aA][nN].*?>", "");
		source = source.replaceAll("</[sS][pP][aA][nN]>", "");

		//防止html中在<>中包括大于号的判断
		//source = source.replaceAll("<[^>'\"]*['\"].*['\"].*?>","");
		source = source.replaceAll("\r\n", "\n");
		source = source.replaceAll("<[^>]*>", "");

		return source;
	
	}

	private String getText()  {

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
			indexDistribution.add(wordsNum<30?0:wordsNum);//存储i后面blocksWidth行的单词数量
			//System.out.println(wordsNum);
		}

		if (indexDistribution.size()<=0) return  null;

		int sum = 0,max=0;

		for (int i=0; i< indexDistribution.size(); i++)
		{
			sum += indexDistribution.get(i);  //用于计算平均每块的单词数
			max = max>indexDistribution.get(i)?max:indexDistribution.get(i);
		}

		int mean=sum/indexDistribution.size();

		/*主题型网页判断*/
		//计算零范数(稀疏度%)
		int L0 = 0;
		for (int i=0; i< indexDistribution.size(); i++)
		{
			L0 += indexDistribution.get(i)>mean?100:0;  //用于计算平均每块的单词数
		}

		L0=L0/indexDistribution.size();

//		int L1 = 0;
//		for (int i=0; i< indexDistribution.size(); i++)
//		{
//			L1 += Math.abs(indexDistribution.get(i)-mean);  //用于计算平均每块的单词数
//		}
//		L1/=indexDistribution.size();

/*		try {
			if (L0 < 3);
		}
		catch (Exception e){

		}*/

/*		if (L0>15) {
			System.out.println("该网页不是主题型的，无法提取正文");
			return null ;
		}*/

		threshold = Math.min(100, (sum/indexDistribution.size())<<(empty/(lines.size()-empty)>>>1)); //>>>同>>，有空行则
		//将阈值升高
		threshold = Math.max(50, threshold);

		threshold=((int) (Math.sqrt(60/L0))+1)*threshold;//可以调整

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
package jp.alhinc.ito_kenta.calculate_sales;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales{
	public static void main(String[] args){

		if(args.length !=1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//宣言
		HashMap<String, String> branchmap = new HashMap<String, String>();
		HashMap<String, String> commoditymap = new HashMap<String, String>();
		HashMap<String, Long> branchmap1 = new HashMap<String, Long>();
		HashMap<String, Long> commoditymap1 = new HashMap<String, Long>();
		List<File> rcdfile = new ArrayList<File>();


		if(!inputFile(args[0],"branch.lst","支店","\\d{3}",branchmap,branchmap1)){
			return;
		}

		if(!inputFile(args[0],"commodity.lst","商品","^[a-zA-Z0-9]{8}$",commoditymap,commoditymap1)){
			return;
		}


		//「売上集計」のディレクトリ内のフォルダ名称を文字列にした後、File型に変換（スキームの確認する）
		String dire = args[0];
		File dir = new File(dire);
		File[] files = dir.listFiles();

		//「売上集計」のディレクトリから、売上ファイル(数字8桁＆末尾.rcd)かつファイル形式のものを抽出する
		for (int i = 0; i < files.length; i++) {

			if(files[i].getName().matches("^\\d{8}.rcd$")&&files[i].isFile()){
				rcdfile.add(files[i]);
			}
		}


		//売上ファイルの連番チェック。差分が1なら連番になっていると考える
		//for文に-1をつけないと、files[i+1]が、無い範囲をしていすることになるので注意。
		for (int i =0; i < rcdfile.size()-1; i++) {

			String index = rcdfile.get(i).getName();
			String str1 = index.substring(1, 8);
			int num = Integer.parseInt(str1);

			String index2 = rcdfile.get(i+1).getName();
			String str2 = index2.substring(1, 8);
			int num2 = Integer.parseInt(str2);


			if(num2-num!=1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}


		//「売上ファイル」をひとつずつ以下の処理
		for (int i = 0; i < rcdfile.size(); i++) {

			BufferedReader br = null;
			try {
				//まず、「売上ファイル」をArrayListに格納する
				ArrayList<String> sold = new ArrayList<String>();
				FileReader sr = new FileReader(rcdfile.get(i));
				br = new BufferedReader(sr);
				String str;
				while((str = br.readLine()) !=null){
					sold.add(str);
				}

				//売上ファイルの中身が3行でない場合エラー
				if(sold.size()!=3){
					System.out.println(rcdfile.get(i).getName()+"のフォーマットが不正です");
					return;
				}

				//売上ファイルの金額部分が数値でなければエラー
				if(!sold.get(2).matches("\\d+")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}


				//売上ファイルの支店コードがbranch.lstに該当がなかった場合エラー
				if(branchmap.get(sold.get(0))==null){
					System.out.println(rcdfile.get(i).getName()+"の支店コードが不正です");
					return;
				}

				//Stirng型のAllayListの金額部分をLong型に変換（=sold1）。
				//branchmap1におけるsold1の支店コードに対応する金額＋上記
				long sold1 = Long.parseLong(sold.get(2));
				long branchsum = branchmap1.get(sold.get(0)) + sold1;


				//branchsumが10桁を超えた場合、エラーにする
				String branchsumSt = Long.toString(branchsum);
				if(!branchsumSt.matches("\\d{1,10}")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchmap1.put(sold.get(0), branchsum);




				//売上ファイルの商品コードがcommodity.lstに該当がなかった場合エラー
				if(commoditymap.get(sold.get(1))==null){
					System.out.println(rcdfile.get(i).getName()+"の商品コードが不正です");
					return;
				}

				//Stirng型のAllayListの金額部分をLong型に変換（=sold2）。
				//commoditymap1におけるsold2の支店コードに対応する金額＋上記がcommoditysum
				long sold2 = Long.parseLong(sold.get(2));
				long commoditysum = commoditymap1.get(sold.get(1)) + sold2;

				//commoditysumが10桁を超えた場合、エラーにする
				String commoditysumSt = Long.toString(commoditysum);
				if(!commoditysumSt.matches("\\d{1,10}")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				commoditymap1.put(sold.get(1), commoditysum);



			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				if(br != null){
					try{
						br.close();
					} catch(IOException e){
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
				}
			}
	    }


		if(!outputFile(branchmap1,branchmap,"branch.out",args[0])){
			return;
		}

		if(!outputFile(commoditymap1,commoditymap,"commodity.out",args[0])){
			return;
		}
	}



	public static boolean inputFile(
			String dirPath,
			String fileName,
			String errorName,
			String format,
			HashMap<String, String> map ,
			HashMap<String, Long> map1
			){

		BufferedReader br = null;
	//まずbranch の処理
		try {
			File file1 = new File(dirPath,fileName);

			//支店定義ファイルがなければエラー
			if(! file1.exists()){
				System.out.println(errorName+"定義ファイルが存在しません");
				return false;
			}

			br = new BufferedReader(new FileReader(file1));
			String s;


			//読み込んだファイルがnullになるまで、一行ずつ繰り返し抽出する
			while((s = br.readLine()) !=null){
				String branch[] = s.split(",");

				//branchの要素数が2でない（=支店名にカンマが含まれない文字列）「数字のみ。3桁固定」でなければエラー
				if(branch.length !=2||!branch[0].matches(format)){
					System.out.println(errorName+"定義ファイルのフォーマットが不正です");
					return false;
				}

				//branchmapに"支店コード"と"支店名"を
				map.put(branch[0], branch[1]);
				//branchmap1に"支店コード"と"売上合計金額(初期値=0)"を入れる
				map1.put(branch[0],(long) 0);
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			if(br != null){
				try{
					br.close();
				} catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}




	public static boolean outputFile(
			HashMap<String, Long> salesmap ,
			HashMap<String, String> defmap ,
			String fileName ,
			String dirPath){


		//支店別集計ファイルを売上金額で降順にする
		List<Map.Entry<String,Long>> entries1 =new ArrayList<Map.Entry<String,Long>>(salesmap.entrySet());
		Collections.sort
			(entries1, new Comparator<Map.Entry<String,Long>>() {

				@Override
				public int compare(Map.Entry<String,Long> entry1, Map.Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});

		BufferedWriter bw = null;
		//支店別集計を降順にしたものを、branch.outファイルに出力する
		try{
			File branchoutfile = new File(dirPath,fileName);
			FileWriter fw = new FileWriter(branchoutfile);
			bw = new BufferedWriter(fw);

			for (Map.Entry<String,Long> s : entries1) {
				bw.write(s.getKey()+","+defmap.get(s.getKey())+","+s.getValue());
				bw.newLine();
			}

		}catch (IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			if(bw != null){
				try{
					bw.close();
				} catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}
}


package Holiday;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class HolidayGraphDegree {
	public static class DegreeMap extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		private Text m_key = new Text();
		private Text m_value = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			// daytype,id1,id2 call,mess
			String line = value.toString();
			String[] info = line.split("\t");
			String[] item = info[0].split(",");
			m_key.set(item[0] + "," + item[1]);
			m_value.set(info[1]);
			output.collect(m_key, m_value);
			m_key.set(item[0] + "," + item[2]);
			output.collect(m_key, m_value);
		}
	}

	public static class DegreeReduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			int cnt[] = {0,0};
			while (values.hasNext()) {
				String info[] = values.next().toString().split(",");
				for(int i=0;i<2;i++){
					cnt[i]+=Integer.parseInt(info[i]);
				}
			}
			output.collect(key, new Text(cnt[0] + "," +cnt[1]));
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = new JobConf(HolidayGraphDegree.class);
		conf.setJobName("GSMNationDayDegreeCount"); // 设置一个用户定义的job名称
		conf.setOutputKeyClass(Text.class); // 为job的输出数据设置Key类
		conf.setOutputValueClass(Text.class); // 为job输出设置value类
		conf.setMapperClass(DegreeMap.class); // 统计度分布
		conf.setReducerClass(DegreeReduce.class); // 为job设置Reduce类
		conf.setInputFormat(TextInputFormat.class); // 为map-reduce任务设置InputFormat实现类
		conf.setOutputFormat(TextOutputFormat.class); // 为map-reduce任务设置OutputFormat实现类
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		JobClient.runJob(conf); // 运行一个job

	}
}
package com.intel.PRCcloud.streamBench.microbench

import com.intel.PRCcloud.streamBench.entity.ParamEntity
import org.apache.spark.streaming.dstream.DStream
import com.intel.PRCcloud.streamBench.metrics.LatencyListener
import org.apache.spark.streaming.StreamingContext
import com.intel.PRCcloud.streamBench.util.BenchLogUtil


class NumericCalcJob(subClassParams:ParamEntity,fieldIndex:Int,separator:String)
  extends RunBenchJobWithInit(subClassParams) {
  class Aggregator(val ValMin:Long, val ValMax:Long, val ValSum:Long, val ValCount:Long) {
    def aggr(v:Aggregator) = {
      val vmin = Math.min(ValMin, v.ValMin)
      val vmax = Math.max(ValMax, v.ValMax)
      val vsum = ValSum + v.ValSum
      val vcount = ValCount + v.ValCount

      new Aggregator(vmin, vmax, vsum, vcount)
    }
  }

  override def processStreamData(lines:DStream[String],ssc:StreamingContext){
    val index=fieldIndex
    val sep=separator

    var SumAggregator = new Aggregator(Int.MaxValue, 0, 0, 0)

    lines.foreachRDD(rdd=>{
      val numbers=rdd.flatMap(line=>{
        val splits=line.split(sep)
        if(index<splits.length)
          Iterator(splits(index).toLong)
        else
          Iterator.empty
      })



      val zero = new Aggregator(Int.MaxValue, 0, 0, 0)
      val curAggregatedValue = numbers.map(x=> new Aggregator(x,x,x,1))
        .fold(zero)((v1,v2) =>v1.aggr(v2))
//
//      val curMax=numbers.fold(0)((v1,v2)=>Math.max(v1, v2))
//      max=Math.max(max, curMax)
//      val curMin=numbers.fold(Int.MaxValue)((v1,v2)=>Math.min(v1, v2))
//      min=Math.min(min, curMin)
//      var curSum=numbers.fold(0)((v1,v2)=>v1+v2)
//      sum+=curSum
//      totalCount+=rdd.count()

      SumAggregator = SumAggregator.aggr(curAggregatedValue)

      BenchLogUtil.logMsg("Current max:"+SumAggregator.ValMax+"  	Time:")
      BenchLogUtil.logMsg("Current min:"+SumAggregator.ValMin+"  	Time:")
      BenchLogUtil.logMsg("Current sum:"+SumAggregator.ValSum+"  	Time:")
      BenchLogUtil.logMsg("Current total:"+SumAggregator.ValCount+"  	Time:")
      BenchLogUtil.logMsg("Current avg:"+(SumAggregator.ValSum.toDouble/SumAggregator.ValCount.toDouble)+"  	Time:")

    })
  }
}


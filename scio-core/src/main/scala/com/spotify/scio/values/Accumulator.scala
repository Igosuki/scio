/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.values

import org.apache.beam.sdk.transforms.Combine.CombineFn
import org.apache.beam.sdk.transforms.Max
import org.apache.beam.sdk.transforms.Min
import org.apache.beam.sdk.transforms.Sum
import org.apache.beam.sdk.transforms.Aggregator

/** Type class for `T` that can be used in an [[Accumulator]]. */
sealed trait AccumulatorType[T] {
  type CF = CombineFn[T, Array[T], T]

  protected def sumFnImpl: CombineFn[_, _, _]
  protected def minFnImpl: CombineFn[_, _, _]
  protected def maxFnImpl: CombineFn[_, _, _]

  /** `CombineFn` for computing sum of the underlying values. */
  def sumFn(): CF = sumFnImpl.asInstanceOf[CF]

  /** `CombineFn` for computing maximum of the underlying values. */
  def minFn(): CF = minFnImpl.asInstanceOf[CF]

  /** `CombineFn` for computing minimum of the underlying values. */
  def maxFn(): CF = maxFnImpl.asInstanceOf[CF]
}

private[scio] class IntAccumulatorType extends AccumulatorType[Int] {
  override protected def sumFnImpl = Sum.ofIntegers()
  override protected def minFnImpl = Min.ofIntegers()
  override protected def maxFnImpl = Max.ofIntegers()
}

private[scio] class LongAccumulatorType extends AccumulatorType[Long] {
  override protected def sumFnImpl = Sum.ofLongs()
  override protected def minFnImpl = Min.ofLongs()
  override protected def maxFnImpl = Max.ofLongs()
}

private[scio] class DoubleAccumulatorType extends AccumulatorType[Double] {
  override protected def sumFnImpl = Sum.ofDoubles()
  override protected def minFnImpl = Min.ofDoubles()
  override protected def maxFnImpl = Max.ofDoubles()
}

/** Encapsulate an accumulator, similar to Hadoop counters. */
trait Accumulator[T] extends Serializable {

  private[scio] val combineFn: CombineFn[T, _, T]

  val name: String

}

/** Encapsulate context of one or more [[Accumulator]]s in an [[SCollectionWithAccumulator]]. */
class AccumulatorContext private[scio] (private val m: Map[String, Aggregator[_, _]])
  extends AnyVal {

  /** Add a value to the given [[Accumulator]]. */
  def addValue[T](acc: Accumulator[T], value: T): AccumulatorContext = {
    m(acc.name).asInstanceOf[Aggregator[T, T]].addValue(value)
    this
  }

}

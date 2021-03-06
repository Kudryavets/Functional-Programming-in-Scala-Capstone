/*
 * Copyright (c) 2014, CleverDATA, LLC. All Rights Reserved.
 *
 * All information contained herein is, and remains the property of CleverDATA, LLC.
 * The intellectual and technical concepts contained herein are proprietary to
 * CleverDATA, LLC. Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written permission is obtained from
 * CleverDATA, LLC.
 *
 * Unless required by applicable law or agreed to in writing, software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * implied.
 */

package observatory

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, Suite}

trait SparkSqlContext extends BeforeAndAfterAll { this: Suite =>
  var sparkSession: SparkSession = _

  override protected def beforeAll() = {
    super.beforeAll()

    sparkSession = SparkSession.builder()
      .master("local")
      .appName("temperature processing")
      .getOrCreate()
  }

  override protected def afterAll() = {
    super.afterAll()
    sparkSession.stop()
  }
}

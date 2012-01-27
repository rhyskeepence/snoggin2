package rhyskeepence.model

case class DataPoint(timestamp: Long, environment: String, metrics: List[Metric])

case class Metric(name: String, value: Double)

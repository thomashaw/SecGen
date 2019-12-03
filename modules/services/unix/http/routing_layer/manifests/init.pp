class routing_layer::init {
  class { '::routing_layer::install': }
  class { '::routing_layer::config': }
  class { '::routing_layer::apache': }
}
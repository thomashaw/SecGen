class routing_layer::init {
  class { '::routing_layer::install': }
  class { '::routing_layer::config': }
}
"""Writes the first N MNIST training images, without their labels."""
import sys

import numpy as np

source, destination, size = sys.argv[1], sys.argv[2], int(sys.argv[3])
with np.load(source, allow_pickle=False) as data:
    np.save(destination, data["x_train"][:size], allow_pickle=False)

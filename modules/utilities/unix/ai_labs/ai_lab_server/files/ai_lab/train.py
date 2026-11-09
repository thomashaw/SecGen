"""Trains the lab's target model from the per-build parameters."""
import json
import os
import shutil
import sys

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")
sys.path.insert(0, "/opt/ai_lab/code")

import numpy as np  # noqa: E402
import tensorflow as tf  # noqa: E402

import common  # noqa: E402


def build_cnn():
    model = tf.keras.Sequential([
        tf.keras.layers.Input((28, 28, 1)),
        tf.keras.layers.Conv2D(32, 3, activation="relu"),
        tf.keras.layers.MaxPooling2D(),
        tf.keras.layers.Conv2D(64, 3, activation="relu"),
        tf.keras.layers.MaxPooling2D(),
        tf.keras.layers.Flatten(),
        tf.keras.layers.Dense(128, activation="relu"),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(10, activation="softmax"),
    ])
    model.compile(optimizer="adam", loss="sparse_categorical_crossentropy", metrics=["accuracy"])
    return model


def train_model_stealing(params):
    """The target learns a secret mapping from digits to category numbers, so a
    plain digit classifier does not agree with it."""
    tf.keras.utils.set_random_seed(params["seed"])
    label_map = np.array(params["label_map"])
    (x_train, y_train), (x_test, y_test) = common.load_mnist()
    model = build_cnn()
    model.fit(common.normalise(x_train), label_map[y_train], epochs=5, batch_size=128, verbose=2)
    _, accuracy = model.evaluate(common.normalise(x_test), label_map[y_test], verbose=0)
    print(f"target accuracy on its own categories: {accuracy:.4f}")
    np.save(common.HIDDEN_IMAGES, x_test)
    return model, {
        "query_budget": params["query_budget"],
        "gradient_endpoint": params["gradient_endpoint"],
        "num_classes": 10,
    }


def train_backdoor(params):
    """No target to attack here: keeps the clean data the evaluator compares
    a student's submission against, and the hidden test set."""
    (x_train, y_train), (x_test, y_test) = common.load_mnist()
    size = params["train_size"]
    np.save(common.TRAIN_IMAGES, x_train[:size])
    np.save(common.TRAIN_LABELS, y_train[:size])
    np.save(common.HIDDEN_IMAGES, x_test)
    np.save(common.HIDDEN_LABELS, y_test)
    return None, {"train_size": size}


TRAINERS = {"model_stealing": train_model_stealing, "backdoor": train_backdoor}


def main():
    params = common.load_params()
    model, public = TRAINERS[params["lab"]](params)
    if model is not None:
        tmp = common.MODEL_FILE + ".partial.keras"
        model.save(tmp)
        shutil.chown(tmp, group="ai_lab")
        os.chmod(tmp, 0o640)
        os.replace(tmp, common.MODEL_FILE)
    # Written last: Puppet treats this file as "training finished".
    with open(common.PUBLIC_PARAMS_FILE, "w") as handle:
        json.dump(public, handle)
    shutil.chown(common.PUBLIC_PARAMS_FILE, group="ai_lab")
    os.chmod(common.PUBLIC_PARAMS_FILE, 0o640)


if __name__ == "__main__":
    main()

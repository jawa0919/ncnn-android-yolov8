import torch


def printDict(d: dict):
    print(f"printDict.start---------------------------------------------------")
    print(f"printDict keys", d.keys())
    print(f"printDict---------------------------------------------------------")
    for key, value in d.items():
        if isinstance(value, dict):
            print(f"{key}: {type(value)} keys {value.keys()}")
        else:
            print(f"{key}: {value} ({type(value)})")
    print(f"printDict.end-----------------------------------------------------")


ptPath = "model/nails_seg_s_yolov8_v1.pt"

ptObject = torch.load(ptPath, map_location="cpu", weights_only=False)

print("type", type(ptObject))
# type <class 'dict'>
print("keys", ptObject.keys())
# keys dict_keys(['epoch', 'best_fitness', 'model', 'ema', 'updates', 'optimizer', 'train_args', 'train_metrics', 'train_results', 'date', 'version'])
# printDict(ptObject)
# 经过排查，有意义的属性有model，train_args，train_metrics，train_results
model = ptObject["model"]
# Type: <class 'ultralytics.nn.tasks.SegmentationModel'>
print("model", type(model))
# print("model", model)
train_args = ptObject["train_args"]
# type <class 'dict'>
print("train_args", type(train_args))
printDict(train_args)
train_metrics = ptObject["train_metrics"]
# type <class 'dict'>
print("train_metrics", type(train_metrics))
printDict(train_metrics)
train_results = ptObject["train_results"]
# type <class 'dict'>
print("train_metrics", type(train_results))
printDict(train_results)

from ultralytics import YOLO


modelPath = "model/nails_seg_s_yolov8_v1.pt"

pt_model = YOLO(modelPath)

pt_model.export(format="ncnn")

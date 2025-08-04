import os

path = "/Users/josh.smith/.kmip/master-key.txt"
file_bytes = os.urandom(96)
with open(path, "wb") as f:
    f.write(file_bytes)
import hashlib

def create_sha256(string):
    sha256 = hashlib.sha256()
    sha256.update(string.encode('utf-8'))
    return sha256.hexdigest()

print(create_sha256("123456"))
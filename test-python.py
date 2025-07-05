import datetime
import socket
import platform

def system_info():
    print("=== Python Test Script ===")
    print(f"Hostname       : {socket.gethostname()}")
    print(f"Platform       : {platform.system()} {platform.release()}")
    print(f"Architecture   : {platform.machine()}")
    print(f"Current Time   : {datetime.datetime.now()}")
    print("==========================")

if __name__ == "__main__":
    system_info()

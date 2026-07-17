import asyncio
import socket
import struct
import base64
import hashlib

GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"


def handshake(headers: str) -> str:
    key = next(line.split(": ")[1].strip() for line in headers.splitlines() if line.lower().startswith("sec-websocket-key"))
    accept = base64.b64encode(hashlib.sha1((key + GUID).encode()).digest()).decode()
    return (
        "HTTP/1.1 101 Switching Protocols\r\n"
        "Upgrade: websocket\r\nConnection: Upgrade\r\n"
        f"Sec-WebSocket-Accept: {accept}\r\n\r\n"
    )


def decode_frame(data: bytes) -> str:
    length = data[1] & 0x7F
    mask = data[2:6]
    payload = data[6:6 + length]
    return bytes(b ^ mask[i % 4] for i, b in enumerate(payload)).decode()


def encode_frame(message: str) -> bytes:
    payload = message.encode()
    return bytes([0x81, len(payload)]) + payload


async def handle(reader, writer):
    headers = (await reader.read(1024)).decode()
    writer.write(handshake(headers).encode())
    await writer.drain()
    try:
        while True:
            data = await reader.read(2048)
            if not data:
                break
            writer.write(encode_frame(decode_frame(data)))
            await writer.drain()
    except ConnectionError:
        pass


async def main():
    server = await asyncio.start_server(handle, "localhost", 8765)
    print("WebSocket echo sur ws://localhost:8765")
    async with server:
        await server.serve_forever()


if __name__ == "__main__":
    asyncio.run(main())

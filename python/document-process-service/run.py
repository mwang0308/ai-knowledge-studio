import uvicorn


def main() -> None:
    """PyCharm 直接运行入口。"""
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8101,
        reload=False,
    )


if __name__ == "__main__":
    main()

from flask import Flask
from app.config import Config
from app.auth import auth_bp
from app.group import group_bp
from app.user import user_bp
from app.client import client_bp
from app.getID import get_bp

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    app.register_blueprint(auth_bp)
    app.register_blueprint(group_bp)
    app.register_blueprint(user_bp)
    app.register_blueprint(client_bp)
    app.register_blueprint(get_bp)

    return app

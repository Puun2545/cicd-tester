from flask import Blueprint, jsonify, request
import aiohttp
from utils.keycloak import get_access_token

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/auth/token', methods=['POST'])
async def get_token():
    token = await get_access_token()
    if token:
        return jsonify({'access_token': token}), 200
    return jsonify({'error': 'Failed to get access token'}), 500

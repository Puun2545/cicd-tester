import aiohttp
import asyncio
from app.config import Config

async def get_access_token():
    url = f'http://{Config.KEYCLOAK_SERVER}/realms/master/protocol/openid-connect/token'
    headers = {'Content-Type': 'application/x-www-form-urlencoded'}
    data = {
        'username': Config.ADMIN_USERNAME,
        'password': Config.ADMIN_PASSWORD,
        'grant_type': 'client_credentials',
        'client_id': Config.CLIENT_ID,
        'client_secret': Config.CLIENT_SECRET
    }
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, data=data) as response:
            response_json = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return response_json.get('access_token')
                except aiohttp.client_exceptions.ContentTypeError:
                    return None
            else:
                raise aiohttp.client_exceptions.ContentTypeError(
                    response.status, response.headers['Content-Type'], response.url
                )

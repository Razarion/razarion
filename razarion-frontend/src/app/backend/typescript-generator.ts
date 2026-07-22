import { HttpClient as HttpClientAdapter, RestResponse } from '../generated/razarion-share';
import { HttpClient, HttpParams } from '@angular/common/http';


export class TypescriptGenerator {
    static generateHttpClientAdapter(httpClient: HttpClient): HttpClientAdapter {
        return new class implements HttpClientAdapter {
            readonly httpClient = httpClient;
            request<R>(requestConfig: { method: string; url: string; queryParams?: any; data?: any; copyFn?: ((data: R) => R) | undefined; }): RestResponse<R> {
              return new Promise((resolve, error) => {
                    this.httpClient.request(requestConfig.method, "/" + requestConfig.url, {
                        body: requestConfig.data,
                        params: TypescriptGenerator.toHttpParams(requestConfig.queryParams)
                    }).subscribe({
                        next: (objectNameIds: any) => {
                            resolve(objectNameIds);
                        },
                        error: (err: any) => {
                            console.log(err);
                            error(err);
                        }
                    });
                });
            }
        }
    }

    /**
     * The generated clients hand their query parameters over as a plain object. Without this they
     * were dropped on the floor, so every endpoint taking a @RequestParam silently ran on its
     * defaults - which is how the daily statistics ignored the platform selector.
     *
     * Undefined and null are skipped rather than sent as the strings "undefined"/"null", and
     * arrays are appended once per element, which is what Spring expects for a list parameter.
     */
    private static toHttpParams(queryParams: any): HttpParams {
        let params = new HttpParams();
        if (!queryParams) {
            return params;
        }
        Object.keys(queryParams).forEach(key => {
            const value = queryParams[key];
            if (value === undefined || value === null) {
                return;
            }
            if (Array.isArray(value)) {
                value.forEach(entry => {
                    if (entry !== undefined && entry !== null) {
                        params = params.append(key, TypescriptGenerator.toParamValue(entry));
                    }
                });
            } else {
                params = params.set(key, TypescriptGenerator.toParamValue(value));
            }
        });
        return params;
    }

    private static toParamValue(value: any): string {
        return value instanceof Date ? value.toISOString() : String(value);
    }
}

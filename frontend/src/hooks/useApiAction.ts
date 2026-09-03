import { useCallback, useState } from 'react'
import { ApiError } from '@/lib/api-client'

export function useApiAction() {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<unknown>(null)
  const [error, setError] = useState<string | null>(null)

  const run = useCallback(async <T,>(action: () => Promise<T>) => {
    setLoading(true)
    setError(null)
    try {
      const data = await action()
      setResult(data)
      return data
    } catch (err) {
      const message =
        err instanceof ApiError
          ? `${err.message}${err.body ? `\n${JSON.stringify(err.body, null, 2)}` : ''}`
          : err instanceof Error
            ? err.message
            : 'Unknown error'
      setError(message)
      setResult(null)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const clear = useCallback(() => {
    setResult(null)
    setError(null)
  }, [])

  return { loading, result, error, run, clear }
}

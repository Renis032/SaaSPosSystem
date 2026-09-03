import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { ApiPlaygroundPage } from '@/pages/ApiPlaygroundPage'

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<ApiPlaygroundPage />} />
      </Routes>
    </BrowserRouter>
  )
}
